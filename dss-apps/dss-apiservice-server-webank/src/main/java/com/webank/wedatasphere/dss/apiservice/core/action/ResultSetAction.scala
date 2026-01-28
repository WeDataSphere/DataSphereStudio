package com.webank.wedatasphere.dss.apiservice.core.action

import org.apache.linkis.common.conf.Configuration
import org.apache.linkis.httpclient.request.GetAction
import org.apache.linkis.ujes.client.exception.UJESClientBuilderException
import org.apache.linkis.ujes.client.request.UJESJobAction

class ResultSetAction private () extends GetAction with UJESJobAction {
  override def suffixURLs: Array[String] = Array("filesystem", "openFile")
}

object ResultSetAction {
  def builder(): Builder = new Builder

  class Builder private[ResultSetAction] () {
    private var user: String = _
    private var path: String = _
    private var page: Int = _
    private var pageSize: Int = _
    private var charset: String = Configuration.BDP_ENCODING.getValue

    // default value is :org.apache.linkis.storage.domain.Dolphin.LINKIS_NULL
    private var nullValue: String = ""

    private var enableLimit: Boolean = false
    private var columnPage: Int = _
    private var columnPageSize: Int = _
    private var truncateColumn: String = _
    private var maskedFieldNames: String = _

    def setUser(user: String): Builder = {
      this.user = user
      this
    }

    def setPath(path: String): Builder = {
      this.path = path
      this
    }

    def setPage(page: Int): Builder = {
      this.page = page
      this
    }

    def setPageSize(pageSize: Int): Builder = {
      this.pageSize = pageSize
      this
    }

    def setCharset(charset: String): Builder = {
      this.charset = charset
      this
    }

    def setNullValue(nullValue: String): Builder = {
      this.nullValue = nullValue
      this
    }

    def setEnableLimit(enableLimit: Boolean): Builder = {
      this.enableLimit = enableLimit
      this
    }

    def setColumnPage(columnPage: Int): Builder = {
      this.columnPage = columnPage
      this
    }
    def setColumnPageSize(columnPageSize: Int): Builder = {
      this.columnPageSize = columnPageSize
      this
    }


    def setTruncateColumn(truncateColumn: String): Builder = {
      this.truncateColumn = truncateColumn
      this
    }

    def setMaskedFieldNames(maskedFieldNames: String): Builder = {
      this.maskedFieldNames = maskedFieldNames
      this
    }


    def build(): ResultSetAction = {
      if (user == null) throw new UJESClientBuilderException("user is needed!")
      if (path == null) throw new UJESClientBuilderException("path is needed!")
      val resultSetAction = new ResultSetAction
      resultSetAction.setParameter("path", path)
      if (page > 0) resultSetAction.setParameter("page", page)
      if (pageSize > 0) resultSetAction.setParameter("pageSize", pageSize)
      resultSetAction.setParameter("charset", charset)
      resultSetAction.setParameter("enableLimit", enableLimit)
      resultSetAction.setParameter("nullValue", nullValue)
      resultSetAction.setParameter("columnPage", columnPage)
      resultSetAction.setParameter("columnPageSize", columnPageSize)
      resultSetAction.setParameter("truncateColumn", truncateColumn)
      resultSetAction.setParameter("maskedFieldNames", maskedFieldNames)
      resultSetAction.setUser(user)
      resultSetAction
    }

  }

}


