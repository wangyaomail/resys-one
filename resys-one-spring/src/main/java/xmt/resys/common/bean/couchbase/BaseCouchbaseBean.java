package xmt.resys.common.bean.couchbase;

import com.couchbase.client.java.document.JsonDocument;

import xmt.resys.common.bean.mongo.BaseIdBean;

public abstract class BaseCouchbaseBean<T> implements BaseIdBean {

    public abstract JsonDocument toJsonDocument(String prefix);

    public abstract T fromDoc(JsonDocument doc,
                              String prefix);

}
