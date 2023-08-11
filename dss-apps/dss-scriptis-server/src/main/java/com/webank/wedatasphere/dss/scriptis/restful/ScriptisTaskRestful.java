package com.webank.wedatasphere.dss.scriptis.restful;

import com.webank.wedatasphere.dss.scriptis.config.DSSScriptisConfiguration;
import com.webank.wedatasphere.dss.scriptis.execute.LinkisJobSubmit;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.security.SecurityFilter;
import org.apache.linkis.ujes.client.UJESClient;
import org.apache.linkis.ujes.client.request.JobDeleteObserveAction;
import org.apache.linkis.ujes.client.request.JobObserveAction;
import org.apache.linkis.ujes.client.response.JobDeleteObserveResult;
import org.apache.linkis.ujes.client.response.JobObserveResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(path = "/dss/scriptis/task", produces = {"application/json"})
public class ScriptisTaskRestful {
    @Autowired
    HttpServletRequest httpServletRequest;
    private static final Logger LOGGER = LoggerFactory.getLogger(ScriptisTaskRestful.class);
    @GetMapping("subscribe")
    public Message subscript(@RequestParam("action") String action,
                             @RequestParam("taskId") Long taskId,
                             @RequestParam("scriptName")String scriptName){
        String title=String.format("脚本%s",scriptName);
        Map<String, String> map = new HashMap<>();
        map.put("title", title);
        map.put("detail", title);
        map.put("alterSysInfo", "[大数据]DSS-IDE(5425)");
        map.put("alterObject", "dss_task");
        String userName=  SecurityFilter.getLoginUsername(httpServletRequest);
        LOGGER.info("{}{}任务通知，taskId:{},scriptName:{}",userName,action,taskId,scriptName);
        UJESClient client= LinkisJobSubmit.getClient();
        Message message;
        if("add".equals(action)){
            JobObserveAction addAction = JobObserveAction.builder()
                    .setUser(userName)
                    .setTaskId(taskId.toString())
                    .setSubSystemId(DSSScriptisConfiguration.SUBSCRIBE_SUBSYSTEM_ID.getValue())
                    //warn级别
                    .setMonitorLevel("4")
                    .setReceiver(userName)
                    .setExtra(map)
                    .build();
            JobObserveResult result;
            try {
                 result = client.addJobObserve(addAction);
            }catch (Exception e){
                LOGGER.error("subscribe failed",e);
                String msg=e.getMessage();
                if(msg!=null&&msg.contains("has been completed")){
                    msg="任务马上完成，可直接查看结果，无需开启通知";
                }
                return Message.error(msg);
            }

            message = result.getStatusCode() == 200 && result.getStatus() == 0 ?
                    Message.ok("成功开启通知，任务结束后您将收到通知。")
                    : Message.error(result.getMessage());
        } else if ("cancel".equals(action)) {
            JobDeleteObserveAction deleteObserveAction=JobDeleteObserveAction.newBuilder().setTaskId(taskId).setUser(userName).build();
            JobDeleteObserveResult result;
            try {
                result = client.deleteJobObserve(deleteObserveAction);
            }catch (Exception e){
                return Message.error(e.getMessage());
            }
            message = result.getStatusCode() == 200 && result.getStatus() == 0 ?
                    Message.ok("成功关闭通知，您将不会收到任务结束通知")
                    : Message.error(result.getMessage());
        }else {
            message= Message.error ("illegal action:" + action);
        }
        return message;
    }
}
