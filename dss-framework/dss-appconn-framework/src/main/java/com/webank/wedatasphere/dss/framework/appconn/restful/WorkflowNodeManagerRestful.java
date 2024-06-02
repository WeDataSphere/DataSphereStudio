package com.webank.wedatasphere.dss.framework.appconn.restful;

import com.webank.wedatasphere.dss.framework.appconn.entity.*;
import com.webank.wedatasphere.dss.framework.appconn.service.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.linkis.server.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Author: xlinliu
 */
@RestController
@RequestMapping(path = "/dss/framework/appconnmanager", produces = {"application/json"})
public class WorkflowNodeManagerRestful {
    @Autowired
    AppConnMenuService appConnMenuService;
    @Autowired
    MenuTypeService menuTypeService;
    @Autowired
    NodeGroupService nodeGroupService;
    @Autowired
    NodeService nodeService;
    @Autowired
    NodeToUiService nodeToUiService;
    @Autowired
    NodeUiService nodeUiService;
    @Autowired
    ValidateService validateService;
    @Autowired
    UiToValidateService uiToValidateService;

    /**
     *  保存工作流节点
     * @param node 节点
     * @return 返回信息，保存后的节点内容，带id

     */
    @RequestMapping(value = "savenode", method= RequestMethod.POST)
    public Message saveNode(
            @RequestBody Node node,
            HttpServletRequest httpReq,
            HttpServletResponse httpResp
    ){
        // 保存节点
        node = nodeService.saveNode(node);
        // 返回保存后的节点信息
        return Message.ok("保存成功").data("node",node);
    }

    /**
     * 获取工作流节点
     * @param nodeId 根据节点id删除
     * @param nodeName 根据节点名删除
     * @return
     */
    @RequestMapping(value = "getnode", method=RequestMethod.GET)
    public Message getNode(
            @RequestParam(value = "nodeId", required = false) String nodeId,
            @RequestParam(value = "nodeName", required = false) String nodeName,
            HttpServletRequest httpReq,
            HttpServletResponse httpResp
    ){
        //nodeId必须为空或者是整数
        if (StringUtils.isNotBlank(nodeId) && !StringUtils.isNumeric(nodeId)){
            return Message.error("nodeId必须为整数");
        }

        List<Node> nodeList = new ArrayList<>();
        // 获取节点
        if(StringUtils.isNotBlank(nodeId)){
            Node node = nodeService.getNodeById(Integer.parseInt(nodeId));
            if(node == null){
                return Message.error("对应id的节点不存在");
            }
            nodeList.add(node);
        }else if(StringUtils.isNotBlank(nodeName)){
            nodeList = nodeService.getNodesByName(nodeName);
        }else{
           nodeList = nodeService.getAllNodes();
        }
        if(!nodeList.isEmpty()){
            List<Integer> nodeIds = nodeList.stream().map(Node::getId).collect(Collectors.toList());
            Map<Integer,Integer> nodeIdGroupIdMap= nodeService.getGroupInfo(nodeIds);
            for (Node node : nodeList) {
                node.setNodeGroup(nodeIdGroupIdMap.get(node.getId()));
            }
        }

        // 返回节点信息
        return Message.ok("获取成功").data("nodeList",nodeList).data("total",nodeList.size());
    }


    /**
     * 新建属性
     * @return
     */
    @RequestMapping(value = "addui", method=RequestMethod.POST)
    public Message addUi(
            @RequestBody NodeUi nodeUi,
            HttpServletRequest httpReq,
            HttpServletResponse httpResp
    ){
        nodeUi=nodeUiService.saveNodeUi(nodeUi);
        return Message.ok("添加成功").data("ui",nodeUi);
    }

    /**
     * 查询属性
     *
     * @param nodeId 查询某个节点的所有属性
     * @param uiId   查询单个属性
     */
    @RequestMapping(value = "getui", method=RequestMethod.GET)
    public Message getui(
            @RequestParam(value = "nodeId", required = false) String nodeId,
            @RequestParam(value = "uiId", required = false) String uiId,
            HttpServletRequest httpReq,
            HttpServletResponse httpResp
    ){
        //nodeId必须为空或者是整数
        if(StringUtils.isNotBlank(nodeId) && !StringUtils.isNumeric(nodeId))
        {
            return Message.error("nodeId必须为空或者是整数");
        }
        //uiId必须为空或者是整数
        if(StringUtils.isNotBlank(uiId) && !StringUtils.isNumeric(uiId)){
            return Message.error("uiId必须为空或者是整数");
        }

        List<NodeUi> uiList = new ArrayList<>();

        if(StringUtils.isNotBlank(uiId)){
            NodeUi nodeUi = nodeUiService.getNodeUiById(Integer.parseInt(uiId));
            if(nodeUi == null){
                return Message.error("未找到对应id的UI");
            }
            uiList.add(nodeUi);
        }else if(StringUtils.isNotBlank(nodeId)){
             uiList = nodeUiService.getNodeUisByNodeId(Integer.parseInt(nodeId));

        }else {
            uiList = nodeUiService.getAllNodeUis();
        }
        return Message.ok("查询成功").data("uiList",uiList).data("total",uiList.size());
    }


    /**
     * 给检点添加属性
     * @return
     */
    @RequestMapping(value = "nodeaddui", method=RequestMethod.POST)
    public Message nodeUiAssociation(
            @RequestBody NodeToUi nodeToUi,
            HttpServletRequest httpReq,
            HttpServletResponse httpResp
    ){
        nodeToUiService.addNodeToUi(nodeToUi);
        return Message.ok("添加成功");
    }

    /**
     * 节点取消关联属性
     *
     */
    @RequestMapping(value = "nodedeleteui", method=RequestMethod.POST)
    public Message removeAttributeFromNode(
            @RequestBody NodeToUi nodeToUi,
            HttpServletRequest httpReq,
            HttpServletResponse httpResp
    ){
        nodeToUiService.removeNodeFromUi(nodeToUi.getNodeId(),nodeToUi.getUiId());
        return Message.ok("删除成功");
    }




    /**
     * 保存校验规则
     *
     */
    @RequestMapping(value = "savevalidate", method=RequestMethod.POST)
    public Message saveValidationRule(
            @RequestBody Validate validate,
            HttpServletRequest httpReq,
            HttpServletResponse httpResp
    ){
        validate= validateService.saveValidation(validate);
        return Message.ok("保存成功").data("validate",validate);
    }

    /**
     * 查询校验规则
     *
     * @param uiId       根据关联的ui查询规则
     * @param validateId 精确查询某一条规则
     */
    @RequestMapping(value = "getuivalidate", method=RequestMethod.GET)
    public Message getuivalidate(
            @RequestParam(value = "uiId", required = false) String uiId,
            @RequestParam(value = "validateId", required = false) String validateId,
            HttpServletRequest httpReq,
            HttpServletResponse httpResp
    ){
        //uiId必须为空或者是整数
        if(StringUtils.isNotBlank(uiId)  && !StringUtils.isNumeric(uiId)){
            return Message.error("uiId必须为空或者是整数");
        }
        //validateId必须为空或者是整数
        if(StringUtils.isNotBlank(validateId) && !StringUtils.isNumeric(validateId)){
            return Message.error("validateId必须为空或者是整数");
        }

        List<Validate> validateList = new ArrayList<>();
         if(validateId!=null){
             Validate validate = validateService.getValidationById(Integer.parseInt(validateId));
             if(validate==null){
                 return Message.error("对应id的校验规则不存在");
             }
             validateList.add(validate);
        }else if(uiId!=null){
            validateList = validateService.getValidationsByUiId(Integer.parseInt(uiId));
        }else{
            validateList = validateService.getAllValidations();
        }
        return Message.ok("查询成功").data("validateList",validateList).data("total",validateList.size());
    }


    /**
     * 属性关联规则
     *
     */
    @RequestMapping(value = "uiaddvalidate", method=RequestMethod.POST)
    public Message attributeAssociationRule(
            @RequestBody UiToValidate uiToValidate,
            HttpServletRequest httpReq,
            HttpServletResponse httpResp
    ){
        uiToValidateService.addUiToValidate(uiToValidate);
        return Message.ok("添加成功");
    }

    /**
     * 属性取消关联规则
     *
     */
    @RequestMapping(value = "uideletevalidate", method=RequestMethod.POST)
    public Message unbindAttributeRule(
            @RequestBody UiToValidate uiToValidate,
            HttpServletRequest httpReq,
            HttpServletResponse httpResp
    ){
        uiToValidateService.removeUiFromValidate(uiToValidate);
        return Message.ok("删除成功");
    }

    /**
     * 删除节点
     *
     * @param nodeId 节点的id
     */
    @RequestMapping(value = "deletenode", method=RequestMethod.POST)
    public Message removeNode(
            @RequestParam(value = "nodeId", required = false) String nodeId,
            @RequestBody(required = false) Map<String, Object> payload,
            HttpServletRequest httpReq,
            HttpServletResponse httpResp
    ){
        if(payload.containsKey("nodeId")){
            nodeId = payload.get("nodeId").toString();
        }
        //如果节点关联了ui，则不允许删除
        if(!nodeUiService.getNodeUisByNodeId(Integer.parseInt(nodeId)).isEmpty()){
            return Message.error("节点关联了UI，不允许删除");
        }

        //nodeId不能为空且必须是整数
        if (StringUtils.isBlank(nodeId) || !StringUtils.isNumeric(nodeId)){
            return Message.error("节点id不能为空且必须是整数");
        }
        nodeService.deleteNode(Integer.parseInt(nodeId));
        return Message.ok("删除成功");
    }

    /**
     * 删除属性
     *
     * @param uiId 属性id
     */
    @RequestMapping(value = "deleteui", method=RequestMethod.POST)
    public Message removeAttribute(
            @RequestParam(value = "uiId", required = false) String uiId,
            @RequestBody(required = false) Map<String, Object> payload,
            HttpServletRequest httpReq,
            HttpServletResponse httpResp
    ){
        if(payload.containsKey("uiId")){
            uiId=payload.get("uiId").toString();
        }
        //uiId必须是一个整数
        if( StringUtils.isBlank(uiId) || !StringUtils.isNumeric(uiId)){
            return Message.error("属性id不能为空且必须是整数");
        }
        //关联检验，属性不能被任何节点关联。
        if(nodeUiService.isUsed(Integer.parseInt(uiId))){
            return Message.error("该属性已被节点关联，不能删除");
        }
        nodeUiService.deleteNodeUi(Integer.parseInt(uiId));
        return Message.ok("删除成功");

    }


    /**
     * 保存菜单
     *
     */
    @RequestMapping(value = "savemenuappconn", method=RequestMethod.POST)
    public Message saveMenu(
            @RequestBody AppConnMenu appConnMenu,
            HttpServletRequest httpReq,
            HttpServletResponse httpResp
    ){
        appConnMenu= appConnMenuService.saveMenu(appConnMenu);
        return Message.ok("保存成功").data("menu",appConnMenu);
    }
    /**
     * 查询appconn的菜单
     *
     * @param appconnId appconn的id
     */
    @RequestMapping(value = "getmenuappconn", method=RequestMethod.GET)
    public Message queryAppconnMenus(
            @RequestParam(value = "appconnId", required = false) String appconnId,
            HttpServletRequest httpReq,
            HttpServletResponse httpResp
    ){
        //appconnId必须是一个整数
        if(StringUtils.isBlank(appconnId)&&StringUtils.isNumeric(appconnId)){
            return Message.error("appconnId必须是一个整数");
        }
        List<AppConnMenu> menus = appConnMenuService.getMenusByAppconnId(Integer.parseInt(appconnId));
        AppConnMenu menu = menus.isEmpty() ? null : menus.get(0);
        return Message.ok("查询成功").data("menu",menu);


    }

    /**
     * 删除appconn的菜单
     *
     * @param appconnId appconn的id
     */
    @RequestMapping(value = "deletemenuappconn", method=RequestMethod.POST)
    public Message deleteAppconnMenu(
            @RequestParam("appconnId") String appconnId,
            @RequestBody(required = false) Map<String, Object> payload,
            HttpServletRequest httpReq,
            HttpServletResponse httpResp
    ){
        if(payload.containsKey("appconnId")){
            appconnId = payload.get("appconnId").toString();
        }
        //appconnId必须是一个整数
        if(StringUtils.isBlank(appconnId)&&StringUtils.isNumeric(appconnId)){
            return Message.error("appconnId必须是一个整数");
        }
        //删除appconn的菜单
        appConnMenuService.deleteMenusByAppconnId(Integer.parseInt(appconnId));
        return Message.ok("删除appconn的菜单成功");

    }


    /**
     * 查询菜单分类枚举值
     *
     */
    @RequestMapping(value = "getmenutype", method=RequestMethod.GET)
    public Message queryMenuCategoryEnums(
            HttpServletRequest httpReq,
            HttpServletResponse httpResp
    ){
        List<MenuType> menuTypes = menuTypeService.getAllMenuTypes();
        return Message.ok().data("menuType", menuTypes).data("total", menuTypes.size());
    }
    /**
     * 查询节点分类枚举值
     *
     */
    @RequestMapping(value = "getnotegroup", method=RequestMethod.GET)
    public Message queryNodeCategoryEnumValues(
            HttpServletRequest httpReq,
            HttpServletResponse httpResp
    ){
       List<NodeGroup> nodeGroups=nodeGroupService.getAllNodeGroups();
        return Message.ok().data("nodeGroup", nodeGroups).data("total", nodeGroups.size());
    }


}



