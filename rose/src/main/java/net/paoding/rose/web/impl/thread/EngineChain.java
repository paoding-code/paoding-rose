package net.paoding.rose.web.impl.thread;

public interface EngineChain {

    public Object doNext() throws Throwable;

    public void addAfterCompletion(AfterCompletion task);

}
