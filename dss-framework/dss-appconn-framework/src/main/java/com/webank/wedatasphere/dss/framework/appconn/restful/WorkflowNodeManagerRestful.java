package com.webank.wedatasphere.dss.framework.appconn.restful;

import com.webank.wedatasphere.dss.framework.appconn.entity.*;
import org.apache.linkis.server.Message;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Author: xlinliu
 */
@RestController
@RequestMapping(path = "/dss/framework/appconnmanager", produces = {"application/json"})
public class WorkflowNodeManagerRestful {

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
        return null;
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
        return null;
    }


    /**
     * 新建属性
     * @return
     */
    @RequestMapping(value = "addui", method=RequestMethod.POST)
    public Message addUi(
            @RequestBody NodeUi request,
            HttpServletRequest httpReq,
            HttpServletResponse httpResp
    ){
        return null;
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
        return null;
    }


    /**
     * 给检点添加属性
     * @return
     */
    @RequestMapping(value = "nodeaddui", method=RequestMethod.POST)
    public Message nodeUiAssociation(
            @RequestBody NodeToUi request,
            HttpServletRequest httpReq,
            HttpServletResponse httpResp
    ){
        return null;
    }

    /**
     * 节点取消关联属性
     *
     */
    @RequestMapping(value = "nodedeleteui", method=RequestMethod.POST)
    public Message removeAttributeFromNode(
            @RequestBody NodeToUi request,
            HttpServletRequest httpReq,
            HttpServletResponse httpResp
    ){
        return null;
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
        return null;
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
        return null;
    }


    /**
     * 属性关联规则
     *
     */
    @RequestMapping(value = "uiaddvalidate", method=RequestMethod.POST)
    public Message attributeAssociationRule(
            @RequestBody UiToValidate request,
            HttpServletRequest httpReq,
            HttpServletResponse httpResp
    ){
        return null;
    }

    /**
     * 属性取消关联规则
     *
     */
    @RequestMapping(value = "uideletevalidate", method=RequestMethod.POST)
    public Message unbindAttributeRule(
            @RequestBody UiToValidate request,
            HttpServletRequest httpReq,
            HttpServletResponse httpResp
    ){
        return null;
    }

    /**
     * 删除节点
     *
     * @param nodeId 节点的id
     */
    @RequestMapping(value = "deletenode", method=RequestMethod.POST)
    public Message removeNode(
            @RequestParam(value = "nodeId", required = false) String nodeId,
            HttpServletRequest httpReq,
            HttpServletResponse httpResp
    ){
        return null;
    }

    /**
     * 删除属性
     *
     * @param uiId 属性id
     */
    @RequestMapping(value = "deleteui", method=RequestMethod.POST)
    public Message removeAttribute(
            @RequestParam(value = "uiId", required = false) String uiId,
            HttpServletRequest httpReq,
            HttpServletResponse httpResp
    ){
        return null;
    }


    /**
     * 保存菜单
     *
     */
    @RequestMapping(value = "savemenuappconn", method=RequestMethod.POST)
    public Message saveMenu(
            @RequestBody AppConnMenu request,
            HttpServletRequest httpReq,
            HttpServletResponse httpResp
    ){
        return null;
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
        return null;
    }

    /**
     * 删除appconn的菜单
     *
     * @param appconnId appconn的id
     */
    @RequestMapping(value = "deletemenuappconn", method=RequestMethod.POST)
    public Message deleteAppconnMenu(
            @RequestParam("appconnId") String appconnId,
            HttpServletRequest httpReq,
            HttpServletResponse httpResp
    ){
        return null;
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
        return null;
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
        return null;
    }


}



