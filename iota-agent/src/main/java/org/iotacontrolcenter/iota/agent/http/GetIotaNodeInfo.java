package org.iotacontrolcenter.iota.agent.http;


public class GetIotaNodeInfo extends HttpPost {

    public GetIotaNodeInfo(String url) {
        super("getIotaNodeInfo", url, "{\"command\": \"getNodeInfo\"}");
        addHeader("Content-Type", "application/json");
    }
}
