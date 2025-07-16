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
package com.webank.wedatasphere.dss.standard.app.development.ref.impl;

import com.webank.wedatasphere.dss.standard.app.development.ref.*;

/**
 * We can find the only difference of these classes between OnlyDevelopmentRequestRef and ThirdlyRequestRef,
 * is that, OnlyDevelopmentRequestRef is used for those third-part AppConns which has no projects, and
 * ThirdlyRequestRef is used for those third-part AppConns which not only contains projects but also contains
 * development process.
 * So, if your system has no project, please use the classes of OnlyDevelopmentRequestRef, otherwise please
 *  use the classes of ThirdlyRequestRef.
 * @author enjoyyin
 * @date 2022-03-09
 * @since 0.5.0
 */
public interface OnlyDevelopmentRequestRef {

    class DSSJobContentRequestRefImpl extends DevelopmentRequestRefImpl<DSSJobContentRequestRefImpl>
            implements DSSJobContentRequestRef<DSSJobContentRequestRefImpl> {}

    class RefJobContentRequestRefImpl extends DevelopmentRequestRefImpl<RefJobContentRequestRefImpl>
            implements RefJobContentRequestRef<RefJobContentRequestRefImpl> {}

    class DSSJobContentWithContextRequestRef extends DevelopmentRequestRefImpl<DSSJobContentWithContextRequestRef>
            implements DSSJobContentRequestRef<DSSJobContentWithContextRequestRef>, DSSContextRequestRef<DSSJobContentWithContextRequestRef> {}

    class RefJobContentWithContextRequestRef extends DevelopmentRequestRefImpl<RefJobContentWithContextRequestRef>
            implements RefJobContentRequestRef<RefJobContentWithContextRequestRef>, DSSContextRequestRef<RefJobContentWithContextRequestRef> {}

    class CopyRequestRefImpl extends DevelopmentRequestRefImpl<CopyRequestRefImpl>
            implements CopyRequestRef<CopyRequestRefImpl> {}

    class CopyWithDSSJobContentRequestRefImpl extends DevelopmentRequestRefImpl<CopyWithDSSJobContentRequestRefImpl>
            implements CopyRequestRef<CopyWithDSSJobContentRequestRefImpl>, DSSJobContentRequestRef<CopyWithDSSJobContentRequestRefImpl> {}

    class CopyWitContextRequestRefImpl extends DevelopmentRequestRefImpl<CopyWitContextRequestRefImpl>
            implements CopyRequestRef<CopyWitContextRequestRefImpl>,
            DSSContextRequestRef<CopyWitContextRequestRefImpl> {}

    class CopyWitContextAndDSSJobContentRequestRefImpl extends DevelopmentRequestRefImpl<CopyWitContextAndDSSJobContentRequestRefImpl>
            implements CopyRequestRef<CopyWitContextAndDSSJobContentRequestRefImpl>,
            DSSContextRequestRef<CopyWitContextAndDSSJobContentRequestRefImpl>,
            DSSJobContentRequestRef<CopyWitContextAndDSSJobContentRequestRefImpl>{}

    class ImportRequestRefImpl extends DevelopmentRequestRefImpl<ImportRequestRefImpl>
            implements ImportRequestRef<ImportRequestRefImpl> {}

    class ImportWithStreamRequestRefImpl extends DevelopmentRequestRefImpl<ImportRequestRefImpl>
            implements ImportRequestRef<ImportRequestRefImpl> {
        @Override
        public boolean isLinkisBMLResources() {
            return false;
        }
    }

    class ImportWitContextRequestRefImpl extends DevelopmentRequestRefImpl<ImportWitContextRequestRefImpl>
            implements ImportRequestRef<ImportWitContextRequestRefImpl>,
            DSSContextRequestRef<ImportWitContextRequestRefImpl>{}

    class ImportWitContextAndStreamRequestRefImpl extends DevelopmentRequestRefImpl<ImportWitContextRequestRefImpl>
            implements ImportRequestRef<ImportWitContextRequestRefImpl>,
            DSSContextRequestRef<ImportWitContextRequestRefImpl> {
        @Override
        public boolean isLinkisBMLResources() {
            return false;
        }
    }

    class UpdateRequestRefImpl extends DevelopmentRequestRefImpl<UpdateRequestRefImpl>
            implements UpdateRequestRef<UpdateRequestRefImpl> {}

    class UpdateWitContextRequestRefImpl extends DevelopmentRequestRefImpl<UpdateWitContextRequestRefImpl>
            implements UpdateRequestRef<UpdateWitContextRequestRefImpl>, DSSContextRequestRef<UpdateWitContextRequestRefImpl>{}

    class QueryJumpUrlRequestRefImpl extends DevelopmentRequestRefImpl<QueryJumpUrlRequestRefImpl>
            implements QueryJumpUrlRequestRef<QueryJumpUrlRequestRefImpl> {}

    class QueryJumpUrlWithContextRequestRefImpl extends DevelopmentRequestRefImpl<QueryJumpUrlWithContextRequestRefImpl>
            implements QueryJumpUrlRequestRef<QueryJumpUrlWithContextRequestRefImpl>,
            DSSContextRequestRef<QueryJumpUrlWithContextRequestRefImpl> {}

    class QueryJumpUrlWithDSSJobContentRequestRefImpl extends DevelopmentRequestRefImpl<QueryJumpUrlWithDSSJobContentRequestRefImpl>
            implements QueryJumpUrlRequestRef<QueryJumpUrlWithDSSJobContentRequestRefImpl>, DSSJobContentRequestRef<QueryJumpUrlWithDSSJobContentRequestRefImpl> {}

    class QueryJumpUrlWithContextAndDSSJobContentRequestRefImpl extends DevelopmentRequestRefImpl<QueryJumpUrlWithContextAndDSSJobContentRequestRefImpl>
            implements QueryJumpUrlRequestRef<QueryJumpUrlWithContextAndDSSJobContentRequestRefImpl>,
            DSSContextRequestRef<QueryJumpUrlWithContextAndDSSJobContentRequestRefImpl>,
            DSSJobContentRequestRef<QueryJumpUrlWithContextAndDSSJobContentRequestRefImpl>{}
}
