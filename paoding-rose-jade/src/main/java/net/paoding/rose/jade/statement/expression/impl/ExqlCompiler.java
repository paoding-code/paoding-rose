package net.paoding.rose.jade.statement.expression.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.paoding.rose.jade.statement.expression.ExqlContext;
import net.paoding.rose.jade.statement.expression.ExqlPattern;
import net.paoding.rose.jade.statement.expression.ExqlUnit;

/**
 * 实现语句编译器。
 * 
 * @author han.liao
 */
public class ExqlCompiler {

    // 编译的常量
    private static final char BRACE_LEFT = '(';

    private static final char BRACE_RIGHT = ')';

    private static final char BLOCK_LEFT = '{';

    private static final char BLOCK_RIGHT = '}';

    private static final String SHARP = "#";

    private static final String JOIN = "!";

    private static final String KEYWORD_IF = "if";

    private static final String KEYWORD_FOR = "for";

    private static final String SHARP_ELSE = "#else";

    // 正则表达式
    private static final Pattern PATTERN_KEYWORD = Pattern.compile( // NL
            "\\:\\:|([\\:\\$]{1}[a-zA-Z0-9_\\.]+)|\\{([^\\{\\}]+)\\}\\?|#(#|!|if|for)?");

    private static final Pattern PATTERN_IN = Pattern.compile(// NL
            "([a-zA-Z0-9_]*)\\s+in\\s+(.+)");

    // 待编译的语句
    private final String pattern;

    private final int length;

    // 编译位置
    private int position = 0;

    /**
     * 创建语句编译器。
     * 
     * @param pattern - 待编译的语句
     */
    public ExqlCompiler(String pattern) {
        this.pattern = pattern;
        this.length = pattern.length();
    }

    /**
     * 执行编译动作。
     * 
     * @return ExqlPattern 对象
     */
    public ExqlPattern compile() {

        // 从语句编译出: ExqlPattern 对象
        return new ExqlPatternImpl(pattern, compileUnit());
    }

    /**
     * 从语句编译出: ExqlUnit 对象。
     * 
     * 
     * @return ExqlUnit 对象
     */
    protected ExqlUnit compileUnit() {

        // 在输入中查找  PREFIX 字符
        Matcher matcher = PATTERN_KEYWORD.matcher(pattern);

        // 输出的单元列表
        ArrayList<ExqlUnit> units = new ArrayList<ExqlUnit>();

        // 组装位置
        int fromIndex = 0;

        while ((position < length) && matcher.find(position)) {

            // 修改当前位置
            position = matcher.end();

            // 检查  :expr | $expr 形式的子句
            String expr = matcher.group(1);
            if (expr != null) {

                // 创建文本子句
                if (matcher.start() > fromIndex) {
                    units.add(new TextUnit(pattern.substring(fromIndex, matcher.start())));
                }

                // 创建  :expr | $expr 形式的子句
                if (expr.charAt(0) == '$') {

                    // 创建  $expr 形式的子句, 作为拼接处理
                    units.add(new JoinExprUnit(expr));
                } else {

                    // 创建  :expr 形式的子句
                    units.add(new ExprUnit(expr));
                }

                fromIndex = position;

                continue;
            }

            // 检查  {...}? 的语法
            String group = matcher.group(2);
            if (group != null) {

                // 创建文本子句
                if (matcher.start() > fromIndex) {
                    units.add(new TextUnit(pattern.substring(fromIndex, matcher.start())));
                }

                // 编译  {...} 内部的子句
                ExqlCompiler compiler = new ExqlCompiler(group);
                ExqlUnit unit = compiler.compileUnit();

                // 创建   {...}? 形式的子句
                units.add(new OptionUnit(unit));

                fromIndex = position;

                continue;
            }

            // 检查  # 后面的关键字
            String keyword = matcher.group(3);

            // 处理  #(:expr) 形式的子句
            if (keyword == null) {

                // 获取括号内的内容
                expr = findBrace(BRACE_LEFT, BRACE_RIGHT);
                if (expr != null) {

                    // 创建文本子句
                    if (matcher.start() > fromIndex) {
                        units.add(new TextUnit(pattern.substring(fromIndex, matcher.start())));
                    }

                    // 创建  #(:expr) 形式的表达式
                    units.add(new ExprUnit(expr));

                    fromIndex = position;
                }
            }
            // 处理  ##(:expr) 形式的子句
            else if (keyword.equals(SHARP) || keyword.equals(JOIN)) {

                // 获取括号内的内容
                expr = findBrace(BRACE_LEFT, BRACE_RIGHT);
                if (expr != null) {

                    // 创建文本子句
                    if (matcher.start() > fromIndex) {
                        units.add(new TextUnit(pattern.substring(fromIndex, matcher.start())));
                    }

                    // 创建  ##(:expr) 形式的表达式
                    units.add(new JoinExprUnit(expr));

                    fromIndex = position;
                }
            }
            // 处理  #if(:expr) {...} #else {...} 形式的子句
            else if (keyword.equals(KEYWORD_IF)) {

                // 获取括号内的内容
                expr = findBrace(BRACE_LEFT, BRACE_RIGHT);
                if (expr != null) {

                    // 编译  {...} 单元
                    ExqlUnit unitIfTrue = compileBlock();
                    if (unitIfTrue != null) {

                        // 创建文本子句
                        if (matcher.start() > fromIndex) {
                            units.add(new TextUnit(pattern.substring(fromIndex, matcher.start())));
                        }

                        ExqlUnit unitIfFalse = null;

                        // 匹配  #else {...} 子句
                        if (match(SHARP_ELSE, position)) {

                            // 编译  {...} 单元
                            unitIfFalse = compileBlock();
                        }

                        // 创建  #if(:expr) {...} #else {...} 形式的子句
                        units.add(new ChoiceUnit(expr, unitIfTrue, unitIfFalse));

                        fromIndex = position;
                    }
                }
            }
            // 处理  #for(variant in :expr) {...} 形式的子句
            else if (keyword.equals(KEYWORD_FOR)) {

                // 获取括号内的内容
                expr = findBrace(BRACE_LEFT, BRACE_RIGHT);
                if (expr != null) {

                    // 编译  {...} 单元
                    ExqlUnit unit = compileBlock();
                    if (unit != null) {

                        // 创建文本子句
                        if (matcher.start() > fromIndex) {
                            units.add(new TextUnit(pattern.substring(fromIndex, matcher.start())));
                        }

                        // 循环变量名
                        String variant = null;

                        // 解析  variant in :expr 表达式
                        Matcher matcherIn = PATTERN_IN.matcher(expr);
                        if (matcherIn.matches()) {
                            variant = matcherIn.group(1);
                            expr = matcherIn.group(2);
                        }

                        // 创建  #for(variant in :expr) {...} 形式的子句
                        units.add(new ForEachUnit(expr, variant, unit));

                        fromIndex = position;
                    }
                }
            }
        }

        if (fromIndex < length) {
            // 写入 PREFIX 字符后的内容。
            units.add(new TextUnit(pattern.substring(fromIndex)));
        }

        if (units.size() > 1) {

            // 返回集合对象
            return new BunchUnit(units);

        } else if (!units.isEmpty()) {

            // 返回单个对象
            return units.get(0);

        } else {

            // 返回空对象
            return new EmptyUnit();
        }
    }

    /**
     * 查找匹配的左括号, 忽略之前的空白字符。
     * 
     * 如果未找到匹配的左括号，函数返回 -1.
     * 
     * @param chLeft - 匹配的左括号
     * @param fromIndex - 查找的起始位置
     * 
     * @return 左括号的位置, 如果未找到匹配的左括号，函数返回 -1.
     */
    private int findLeftBrace(char chLeft, int fromIndex) {

        // 查找出现的左括号。
        for (int index = fromIndex; index < length; index++) {

            char ch = pattern.charAt(index);

            // 如果出现左括号，返回。
            if (ch == chLeft) {
                return index;
            }
            // 如果出现非空白字符，返回 - 1.
            else if (!Character.isWhitespace(ch)) {
                return -1;
            }
        }

        // 没有找到匹配的括号。
        return -1;
    }

    /**
     * 查找匹配的右括号, 可以用于匹配 '{}', '[]', '()' 括号对。
     * 
     * 如果未找到匹配的右括号，函数返回 -1.
     * 
     * @param string - 查找的字符串
     * @param chLeft - 匹配的左括号
     * @param chRight - 匹配的右括号
     * @param fromIndex - 查找的起始位置
     * 
     * @return 右括号的位置, 如果未找到匹配的右括号，函数返回 -1.
     */
    private int findRightBrace(char chLeft, char chRight, int fromIndex) {

        int level = 0; // 记录括号重叠级别。

        // 查找匹配的右括号。
        for (int index = fromIndex; index < length; index++) {

            char ch = pattern.charAt(index);

            // 如果出现左括号，重叠级别增加。
            if (ch == chLeft) {
                level++;
            }
            // 如果出现右括号，重叠级别降低。
            else if (ch == chRight) {

                if (level == 0) {
                    // 找到右括号。
                    return index;
                }

                level--;
            }
        }

        // 没有找到匹配的括号。
        return -1;
    }

    /**
     * 从当前位置查找匹配的一对括号, 并返回内容。
     * 
     * 如果有匹配的括号, 返回后的当前位置指向匹配的右括号后一个字符。
     * 
     * @param chLeft - 匹配的左括号
     * @param chRight - 匹配的右括号
     * 
     * @return 返回括号内容, 如果没有括号匹配, 返回 <code>null</code>.
     */
    private String findBrace(char chLeft, char chRight) {

        // 从当前位置查找查找匹配的  (...)
        int left = findLeftBrace(chLeft, position);
        if (left >= position) {

            int start = left + 1;
            int end = findRightBrace(chLeft, chRight, start);
            if (end >= start) {

                // 当前位置指向匹配的右括号后一个字符
                position = end + 1;

                // 返回匹配的括号内容
                return pattern.substring(start, end);
            }
        }

        return null; // 没有  (...) 匹配
    }

    /**
     * 从当前位置查找匹配的: {...} 并编译出: ExqlUnit 对象。
     * 
     * 如果有匹配的: {...}, 编译后, 当前位置指向匹配的右括号后一个字符。
     * 
     * @return ExqlUnit, 如果没有: {...} 匹配, 返回 <code>null</code>.
     */
    private ExqlUnit compileBlock() {

        // 查找匹配的  {...}
        String group = findBrace(BLOCK_LEFT, BLOCK_RIGHT);
        if (group != null) {

            // 编译  {...} 内部的子句
            ExqlCompiler compiler = new ExqlCompiler(group);
            return compiler.compileUnit();
        }

        return null; // 没有  {...} 匹配
    }

    /**
     * 匹配指定的关键字。
     * 
     * 如果匹配成功, 当前位置是关键字最后一个字符之后。
     * 
     * @param keyword - 匹配的关键字
     * @param fromIndex - 查找的起始位置
     * 
     * @return true / false
     */
    private boolean match(String keyword, int fromIndex) {

        int match = 0;

        // 查找非空白字符。
        for (int index = fromIndex; index < length; index++) {

            char ch = pattern.charAt(index);

            if (!Character.isWhitespace(ch)) {
                match = index;
                break;
            }
        }

        // 匹配关键字内容。
        for (int index = 0; index < keyword.length(); index++) {

            char ch = pattern.charAt(match);

            if (ch != keyword.charAt(index)) {
                // 与关键字不匹配
                return false;
            }

            match++;
        }

        // 修改当前位置
        position = match;

        return true;
    }

    // 进行简单测试
    public static void main(String... args) throws Exception {

        String string = "SELECT :expr1, #($expr2.class),"
                + " WHERE #if(:expr3) {e = $expr3} #else {e IS NULL}"
                + "#for(variant in $expr4.bytes) { AND c = :variant}" // NL
                + " {AND d = :expr5}? {AND f = $expr6}?" // NL
                + " BY #!(:expr7) ASC";

        // 在输入中查找  PREFIX 字符
        Matcher matcher = PATTERN_KEYWORD.matcher(string);

        int position = 0;

        while (matcher.find(position)) {

            System.out.println("===============================");
            System.out.println("group 0: " + matcher.group(0));
            System.out.println("group 1: " + matcher.group(1));
            System.out.println("group 2: " + matcher.group(2));
            System.out.println("group 3: " + matcher.group(3));

            position = matcher.end();
        }

        // 在循环表达式中查找
        matcher = PATTERN_IN.matcher("variant in :expr5");

        if (matcher.matches()) {

            System.out.println("===============================");
            System.out.println("group 0: " + matcher.group(0));
            System.out.println("group 1: " + matcher.group(1));
            System.out.println("group 2: " + matcher.group(2));
        }

        // 编译下列语句
        ExqlPattern pattern = new ExqlCompiler(string).compile();

        ExqlContext context = new ExqlContextImpl(string.length());

        HashMap<String, Object> map = new HashMap<String, Object>();

        map.put("expr1", "expr1");
        map.put("expr2", "expr2");
        map.put("expr3", "expr3");
        map.put("expr4", "expr4");
        map.put("expr5", "expr5");
        // map.put("expr6", "expr6");
        map.put("expr7", "expr7");

        System.out.println(pattern.execute(context, map, map));
        System.out.println(Arrays.toString(context.getParams()));
    }
}
