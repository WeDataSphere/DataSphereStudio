/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.webank.wedatasphere.dss.appconn.sendemail.cs


import java.util

import com.google.gson.internal.LinkedTreeMap
import com.webank.wedatasphere.dss.appconn.sendemail.exception.EmailSendFailedException
import com.webank.wedatasphere.dss.standard.app.development.listener.core.ExecutionRequestRefContext
import org.apache.linkis.common.utils.Logging
import org.apache.linkis.cs.client.service.LinkisJobDataServiceImpl
import org.apache.linkis.cs.client.utils.{ContextServiceUtils, SerializeHelper}
import org.apache.linkis.cs.common.entity.enumeration.{ContextScope, ContextType}
import org.apache.linkis.cs.common.entity.source.CommonContextKey
import org.apache.linkis.cs.common.utils.CSCommonUtils
import org.apache.linkis.server.JSONUtils
import org.springframework.util.CollectionUtils

import scala.collection.JavaConversions._


object EmailCSHelper extends Logging{

  private val JOB_IDS_CACHE_KEY = "__dss_sendemail_job_ids_cache__"

  private case class JobIdsCache(jobIds: Array[Long])

  /**
    * update by peaceWong form cs to get job ID
    */
  def getJobIds(refContext: ExecutionRequestRefContext): Array[Long] = {
    val runtimeMap = refContext.getRuntimeMap
    runtimeMap.synchronized {
      runtimeMap.get(JOB_IDS_CACHE_KEY) match {
        case cache: JobIdsCache =>
          val cachedJobIds = cache.jobIds.clone()
          info(s"From sendemail execution cache to get Job IDs ${cachedJobIds.toList}.")
          cachedJobIds
        case _ =>
          val jobIds = loadJobIds(refContext)
          runtimeMap.put(JOB_IDS_CACHE_KEY, JobIdsCache(jobIds.clone()))
          jobIds
      }
    }
  }

  private[sendemail] def clearJobIdsCache(refContext: ExecutionRequestRefContext): Unit = {
    if (refContext != null && refContext.getRuntimeMap != null) {
      val runtimeMap = refContext.getRuntimeMap
      runtimeMap.synchronized {
        runtimeMap.remove(JOB_IDS_CACHE_KEY)
      }
    }
  }

  private def loadJobIds(refContext: ExecutionRequestRefContext): Array[Long] = {
    val runtimeMap = refContext.getRuntimeMap
    val contextIDStr = ContextServiceUtils.getContextIDStrByMap(runtimeMap)
    val nodeIDs = runtimeMap.get("content") match {
      case string: String => JSONUtils.gson.fromJson(string, classOf[java.util.List[String]])
      case list: java.util.List[String] => list
    }
    if (null == nodeIDs || nodeIDs.length < 1){
      throw new EmailSendFailedException(80003 ,"empty result set is not allowed")
    }
    info(s"From cs to getJob ids $nodeIDs.")
    info(s"The contextID value is $contextIDStr")
    val jobIds = nodeIDs.map { nodeId =>
      val nodeName = ContextServiceUtils.getNodeNameByNodeID(contextIDStr, nodeId)
      if (nodeName == null) {
        info(s"The node name is null")
      } else {
        info(s"The node name is $nodeName")
      }
      nodeName
    }.map{ nodeName =>
      val contextKey = new CommonContextKey
      contextKey.setContextScope(ContextScope.PUBLIC)
      contextKey.setContextType(ContextType.DATA)
      contextKey.setKey(CSCommonUtils.NODE_PREFIX + nodeName + CSCommonUtils.JOB_ID)
      LinkisJobDataServiceImpl.getInstance().getLinkisJobData(contextIDStr, SerializeHelper.serializeContextKey(contextKey))
    }.map(_.getJobID).toArray
    if (null == jobIds || jobIds.length < 1){
      throw new EmailSendFailedException(80003 ,"empty result set is not allowed")
    }
    info(s"Job IDs is ${jobIds.toList}.")
    jobIds
  }

  def getJobTypes(refContext: ExecutionRequestRefContext):util.ArrayList[String] = {
    val jobTypes = new util.ArrayList[String]()
    getJobIds(refContext).foreach { jobId =>
      jobTypes.add(refContext.fetchLinkisJob(jobId).getParams.get("labels").asInstanceOf[LinkedTreeMap[_,_]].get("codeType").toString)
    }
    if (CollectionUtils.isEmpty(jobTypes)) {
      throw new EmailSendFailedException(80003 ,"empty result set is not allowed")
    }
    info(s"Job Types is $jobTypes.")
    jobTypes
  }
}
