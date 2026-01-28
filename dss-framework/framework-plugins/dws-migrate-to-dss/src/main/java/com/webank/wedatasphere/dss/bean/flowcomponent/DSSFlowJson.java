package com.webank.wedatasphere.dss.bean.flowcomponent;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.webank.wedatasphere.dss.GsonUtils;
import com.webank.wedatasphere.dss.common.exception.DSSRuntimeException;
import com.webank.wedatasphere.dss.common.utils.DSSCommonUtils;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * dss 工作流
 * Author: xlinliu
 * Date: 2023/10/12
 */
public class DSSFlowJson {
    private List<Edge> edges = new ArrayList<>();

    private List<DSSCommonNode> nodes = new ArrayList<>();
    private String comment;
    private String type;
    private Long updateTime;
    private String updateUser;
    private List<Map<String, String>> props = new ArrayList<>(0);
    private List<Map<String, Object>> resources = new ArrayList<>(0);
    private Map<String, String> scheduleParams = new HashMap<>();
    /**
     * 只需要有个key就行，值在导入的时候会自动生成。
     */
    private String contextID = "";
    /**
     * 只需要有个key就行，值在导入的时候会自动生成。
     */
    private String orcVersion="";
    private String schedulerAppConnName = "schedulis";

    public List<Edge> getEdges() {
        return edges;
    }

    public void setEdges(List<Edge> edges) {
        this.edges = edges;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public String getUpdateUser() {
        return updateUser;
    }

    public void setUpdateUser(String updateUser) {
        this.updateUser = updateUser;
    }

    public List<Map<String, String>> getProps() {
        return props;
    }

    public void setProps(List<Map<String, String>> props) {
        this.props = props;
    }

    public List<Map<String, Object>> getResources() {
        return resources;
    }

    public void setResources(List<Map<String, Object>> resources) {
        this.resources = resources;
    }

    public Map<String, String> getScheduleParams() {
        return scheduleParams;
    }

    public void setScheduleParams(Map<String, String> scheduleParams) {
        this.scheduleParams = scheduleParams;
    }

    public String getContextID() {
        return contextID;
    }

    public void setContextID(String contextID) {
        this.contextID = contextID;
    }

    public String getOrcVersion() {
        return orcVersion;
    }

    public void setOrcVersion(String orcVersion) {
        this.orcVersion = orcVersion;
    }

    public String getSchedulerAppConnName() {
        return schedulerAppConnName;
    }

    public void setSchedulerAppConnName(String schedulerAppConnName) {
        this.schedulerAppConnName = schedulerAppConnName;
    }

    public List<DSSCommonNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<DSSCommonNode> nodes) {
        this.nodes = nodes;
    }

    public static DSSCommonNode convertNode(JsonObject dwsNode, long modifyTime, String modifyUser){
        String dwsType = GsonUtils.getString(dwsNode,"jobType");
        switch (dwsType){
            case "ujes.shell": return DSSShellNode.fromDwsNode(dwsNode,modifyTime,modifyUser);
            case "wtss.connector": return DSSConnectorNode.fromDwsNode(dwsNode,modifyTime,modifyUser);
            case "flow": return DSSSubflowNode.fromDwsNode(dwsNode,modifyTime,modifyUser);
            case "wtss.eventcheckerf": return DSSEventSenderNode.fromDwsNode(dwsNode,modifyTime,modifyUser);
            case "wtss.eventcheckerw": return DSSEventReceiverNode.fromDwsNode(dwsNode,modifyTime,modifyUser);
            case "wtss.datachecker": return DSSDataCheckerNode.fromDwsNode(dwsNode,modifyTime,modifyUser);
            default:
                throw new DSSRuntimeException("unknown node type"+dwsType);
        }
    }

    /**
     * 把节点的id和key换成uuid
     */
    private void processIdKeyToUUID(){
        List<DSSCommonNode> nodes=this.getNodes();
        Map<String, String> oldNewId = new HashMap<>(nodes.size());
        for (DSSCommonNode node : nodes) {
            String oldId = node.getId();;
            if (StringUtils.isEmpty(oldId)) {
                throw new DSSRuntimeException("node id is empty, update key and id failed, node name:" + node.getTitle());
            }
            String uuid = UUID.randomUUID().toString();
            oldNewId.put(oldId, uuid);
            node.setKey(uuid);
            node.setId(uuid);
        }

        for (Edge edge : this.getEdges()) {
            String source=edge.getSource();
            String target = edge.getTarget();
            if(oldNewId.containsKey(source)){
                edge.setSource(oldNewId.get(source));
            }
            if(oldNewId.containsKey(target)){
                edge.setTarget(oldNewId.get(target));
            }
        }

    }

    public static DSSFlowJson fromDwsFlowJson(JsonObject dwsFlowJson,String updateUser){
        DSSFlowJson dssFlowJson = DSSCommonUtils.COMMON_GSON.fromJson(dwsFlowJson, DSSFlowJson.class);
        Long modifyTime =dssFlowJson.getUpdateTime();
        //处理代理用户的key名
        Iterator<Map<String, String>> propsIterator = dssFlowJson.getProps().iterator();
        Map<String,String> proxyUser=Collections.singletonMap("user.to.proxy",updateUser);
        while (propsIterator.hasNext()){
            Map<String,String> prop=propsIterator.next();
            if(prop.containsKey("wtss.flow.proxy.user")){
                proxyUser = Collections.singletonMap("user.to.proxy", prop.get("wtss.flow.proxy.user"));
                propsIterator.remove();
                break;
            }
        }
        dssFlowJson.getProps().add(proxyUser);
        dssFlowJson.getScheduleParams().put("proxyuser",proxyUser.get("user.to.proxy"));

        //处理updateUser
        dssFlowJson.setUpdateUser(updateUser);

        //处理节点
        List<DSSCommonNode> nodes = new ArrayList<>();
        for (JsonElement node :dwsFlowJson.getAsJsonArray("nodes")) {
            JsonObject nodeObj=node.getAsJsonObject();
            DSSCommonNode dssNode = convertNode(nodeObj,modifyTime,updateUser);
            nodes.add(dssNode);

        }
        dssFlowJson.setNodes(nodes);
        dssFlowJson.processIdKeyToUUID();
        return dssFlowJson;
    }
}
