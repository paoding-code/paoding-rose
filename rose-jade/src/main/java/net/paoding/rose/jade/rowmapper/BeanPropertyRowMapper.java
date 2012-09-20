/*
 * Copyright 2002-2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.paoding.rose.jade.rowmapper;

import java.beans.PropertyDescriptor;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.NotWritablePropertyException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.util.Assert;

/**
 * {@link RowMapper} implementation that converts a row into a new instance
 * of the specified mapped target class. The mapped target class must be a
 * top-level class and it must have a default or no-arg constructor.
 * 
 * <p>
 * Column values are mapped based on matching the column name as obtained
 * from result set metadata to public setters for the corresponding
 * properties. The names are matched either directly or by transforming a
 * name separating the parts with underscores to the same name using
 * "camel" case.
 * 
 * <p>
 * Mapping is provided for fields in the target class for many common
 * types, e.g.: String, boolean, Boolean, byte, Byte, short, Short, int,
 * Integer, long, Long, float, Float, double, Double, BigDecimal,
 * <code>java.util.Date</code>, etc.
 * 
 * <p>
 * To facilitate mapping between columns and fields that don't have
 * matching names, try using column aliases in the SQL statement like
 * "select fname as first_name from customer".
 * 
 * <p>
 * Please note that this class is designed to provide convenience rather
 * than high performance. For best performance consider using a custom
 * RowMapper.
 * 
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @author 王志亮 [qieqie.wang@gmail.com]
 * @since 2.5
 */
public class BeanPropertyRowMapper implements RowMapper {

    /** Logger available to subclasses */
    protected final Log logger = LogFactory.getLog(getClass());

    /** The class we are mapping to */
    private final Class<?> mappedClass;

    /** Map of the fields we provide mapping for */
    private Map<String, PropertyDescriptor> mappedFields;

    private final boolean checkColumns;

    private final boolean checkProperties;

    /** Set of bean properties we provide mapping for */
    private Set<String> mappedProperties;

    /**
     * Create a new BeanPropertyRowMapper, accepting unpopulated properties
     * in the target bean.
     * 
     * @param mappedClass the class that each row should be mapped to
     */
    public BeanPropertyRowMapper(Class<?> mappedClass, boolean checkColumns, boolean checkProperties) {
        this.mappedClass = mappedClass;
        Assert.state(this.mappedClass != null, "Mapped class was not specified");
        this.checkProperties = checkProperties;
        this.checkColumns = checkColumns;
        initialize();
    }

    /**
     * Initialize the mapping metadata for the given class.
     * 
     * @param mappedClass the mapped class.
     */
    protected void initialize() {
        this.mappedFields = new HashMap<String, PropertyDescriptor>();
        PropertyDescriptor[] pds = BeanUtils.getPropertyDescriptors(mappedClass);
        if (checkProperties) {
            mappedProperties = new HashSet<String>();
        }
        for (int i = 0; i < pds.length; i++) {
            PropertyDescriptor pd = pds[i];
            if (pd.getWriteMethod() != null) {
                if (checkProperties) {
                    this.mappedProperties.add(pd.getName());
                }
                this.mappedFields.put(pd.getName().toLowerCase(), pd);
                for (String underscoredName : underscoreName(pd.getName())) {
                    if (underscoredName != null
                            && !pd.getName().toLowerCase().equals(underscoredName)) {
                        this.mappedFields.put(underscoredName, pd);
                    }
                }
            }
        }
    }

    /**
     * Convert a name in camelCase to an underscored name in lower case.
     * Any upper case letters are converted to lower case with a preceding
     * underscore.
     * 
     * @param camelCaseName the string containing original name
     * @return the converted name
     */
    private String[] underscoreName(String camelCaseName) {
        StringBuilder result = new StringBuilder();
        if (camelCaseName != null && camelCaseName.length() > 0) {
            result.append(camelCaseName.substring(0, 1).toLowerCase());
            for (int i = 1; i < camelCaseName.length(); i++) {
                char ch = camelCaseName.charAt(i);
                if (Character.isUpperCase(ch)) {
                    result.append("_");
                    result.append(Character.toLowerCase(ch));
                } else {
                    result.append(ch);
                }
            }
        }
        String name = result.toString();
        // 当name为user1_name2时，使name2为user_1_name_2
        // 这使得列user_1_name_2的列能映射到user1Name2属性
        String name2 = null;
        boolean digitFound = false;
        for (int i = name.length() - 1; i >= 0; i--) {
            if (Character.isDigit(name.charAt(i))) {
            	// 遇到数字就做一个标识并continue,直到不是时才不continue
                digitFound = true;
                continue;
            }
            // 只有上一个字符是数字才做下划线
            if (digitFound && i < name.length() - 1 && i > 0) {
                if (name2 == null) {
                    name2 = name;
                }
                name2 = name2.substring(0, i + 1) + "_" + name2.substring(i + 1);
            }
            digitFound = false;
        }
        return new String[] { name, name2 };
    }

    /**
     * Extract the values for all columns in the current row.
     * <p>
     * Utilizes public setters and result set metadata.
     * 
     * @see java.sql.ResultSetMetaData
     */
    public Object mapRow(ResultSet rs, int rowNumber) throws SQLException {
        // spring's : Object mappedObject = BeanUtils.instantiateClass(this.mappedClass);
        // jade's : private Object instantiateClass(this.mappedClass);
        // why: 经过简单的笔记本测试，mappedClass.newInstrance性能比BeanUtils.instantiateClass(mappedClass)快1个数量级
        Object mappedObject = instantiateClass(this.mappedClass);
        BeanWrapper bw = new BeanWrapperImpl(mappedObject);

        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();

        boolean warnEnabled = logger.isWarnEnabled();
        boolean debugEnabled = logger.isDebugEnabled();
        Set<String> populatedProperties = (checkProperties ? new HashSet<String>() : null);

        for (int index = 1; index <= columnCount; index++) {
            String column = JdbcUtils.lookupColumnName(rsmd, index).toLowerCase();
            PropertyDescriptor pd = this.mappedFields.get(column);
            if (pd != null) {
                try {
                    Object value = JdbcUtils.getResultSetValue(rs, index, pd.getPropertyType());
                    if (debugEnabled && rowNumber == 0) {
                        logger.debug("Mapping column '" + column + "' to property '" + pd.getName()
                                + "' of type " + pd.getPropertyType());
                    }
                    bw.setPropertyValue(pd.getName(), value);
                    if (populatedProperties != null) {
                        populatedProperties.add(pd.getName());
                    }
                } catch (NotWritablePropertyException ex) {
                    throw new DataRetrievalFailureException("Unable to map column " + column
                            + " to property " + pd.getName(), ex);
                }
            } else {
                if (checkColumns) {
                    throw new InvalidDataAccessApiUsageException("Unable to map column '" + column
                            + "' to any properties of bean " + this.mappedClass.getName());
                }
                if (warnEnabled && rowNumber == 0) {
                    logger.warn("Unable to map column '" + column + "' to any properties of bean "
                            + this.mappedClass.getName());
                }
            }
        }

        if (populatedProperties != null && !populatedProperties.equals(this.mappedProperties)) {
            throw new InvalidDataAccessApiUsageException(
                    "Given ResultSet does not contain all fields "
                            + "necessary to populate object of class [" + this.mappedClass + "]: "
                            + this.mappedProperties);
        }

        return mappedObject;
    }

    /**
     * 
     * @param clazz
     * @return
     * @throws BeanInstantiationException
     * @see {@link BeanUtils#instantiateClass(Class)}
     */
    private static Object instantiateClass(Class<?> clazz) throws BeanInstantiationException {
        /*- spring's BeanUtils.instantiateClass()
         Assert.notNull(clazz, "Class must not be null");
        if (clazz.isInterface()) {
            throw new BeanInstantiationException(clazz, "Specified class is an interface");
        }
        try {
            return instantiateClass(clazz.getDeclaredConstructor((Class[]) null), null);
        }
        catch (NoSuchMethodException ex) {
            throw new BeanInstantiationException(clazz, "No default constructor found", ex);
        }*/

        try {
            return clazz.newInstance();
        } catch (Exception ex) {
            throw new BeanInstantiationException(clazz, ex.getMessage(), ex);
        }
    }

}
