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
 * <br>
 * So, if your system has no project, please use the classes of OnlyDevelopmentRequestRef, otherwise please
 *  use the classes of ThirdlyRequestRef.
 * @author enjoyyin
 * @date 2022-03-09
 * @since 0.5.0
 */
public interface ThirdlyRequestRef {

    class DSSJobContentRequestRefImpl extends DevelopmentRequestRefImpl<DSSJobContentRequestRefImpl>
            implements DSSJobContentRequestRef<DSSJobContentRequestRefImpl>,
            ProjectRefRequestRef<DSSJobContentRequestRefImpl> {}

    class DSSJobContentWithContextRequestRef extends DevelopmentRequestRefImpl<DSSJobContentWithContextRequestRef>
            implements DSSJobContentRequestRef<DSSJobContentWithContextRequestRef>, DSSContextRequestRef<DSSJobContentWithContextRequestRef>,
            ProjectRefRequestRef<DSSJobContentWithContextRequestRef> {}

    class RefJobContentRequestRefImpl extends DevelopmentRequestRefImpl<RefJobContentRequestRefImpl>
            implements RefJobContentRequestRef<RefJobContentRequestRefImpl>, ProjectRefRequestRef<RefJobContentRequestRefImpl> {}

    class RefJobContentWithContextRequestRef extends DevelopmentRequestRefImpl<RefJobContentWithContextRequestRef>
            implements RefJobContentRequestRef<RefJobContentWithContextRequestRef>, DSSContextRequestRef<RefJobContentWithContextRequestRef>,
            ProjectRefRequestRef<RefJobContentWithContextRequestRef> {}

    class CopyRequestRefImpl extends DevelopmentRequestRefImpl<CopyRequestRefImpl>
            implements CopyRequestRef<CopyRequestRefImpl>, ProjectRefRequestRef<CopyRequestRefImpl> {}

    class CopyWithDSSJobContentRequestRefImpl extends DevelopmentRequestRefImpl<CopyWithDSSJobContentRequestRefImpl>
            implements CopyRequestRef<CopyWithDSSJobContentRequestRefImpl>, ProjectRefRequestRef<CopyWithDSSJobContentRequestRefImpl>,
            DSSJobContentRequestRef<CopyWithDSSJobContentRequestRefImpl> {}

    class CopyWitContextRequestRefImpl extends DevelopmentRequestRefImpl<CopyWitContextRequestRefImpl>
            implements CopyRequestRef<CopyWitContextRequestRefImpl>, ProjectRefRequestRef<CopyWitContextRequestRefImpl>,
            DSSContextRequestRef<CopyWitContextRequestRefImpl> {}

    class CopyWitContextAndDSSJobContentRequestRefImpl extends DevelopmentRequestRefImpl<CopyWitContextAndDSSJobContentRequestRefImpl>
            implements CopyRequestRef<CopyWitContextAndDSSJobContentRequestRefImpl>,
            DSSContextRequestRef<CopyWitContextAndDSSJobContentRequestRefImpl>, ProjectRefRequestRef<CopyWitContextAndDSSJobContentRequestRefImpl>,
            DSSJobContentRequestRef<CopyWitContextAndDSSJobContentRequestRefImpl>{}

    class ImportRequestRefImpl extends DevelopmentRequestRefImpl<ImportRequestRefImpl>
            implements ImportRequestRef<ImportRequestRefImpl>, ProjectRefRequestRef<ImportRequestRefImpl> {}

    class ImportWithStreamRequestRefImpl extends DevelopmentRequestRefImpl<ImportWithStreamRequestRefImpl>
            implements ImportRequestRef<ImportWithStreamRequestRefImpl>, ProjectRefRequestRef<ImportWithStreamRequestRefImpl> {
        @Override
        public boolean isLinkisBMLResources() {
            return false;
        }
    }

    class ImportWitContextRequestRefImpl extends DevelopmentRequestRefImpl<ImportWitContextRequestRefImpl>
            implements ImportRequestRef<ImportWitContextRequestRefImpl>, ProjectRefRequestRef<ImportWitContextRequestRefImpl>,
            DSSContextRequestRef<ImportWitContextRequestRefImpl>{}

    class ImportWitContextAndStreamRequestRefImpl extends DevelopmentRequestRefImpl<ImportWitContextAndStreamRequestRefImpl>
            implements ImportRequestRef<ImportWitContextAndStreamRequestRefImpl>, ProjectRefRequestRef<ImportWitContextAndStreamRequestRefImpl>,
            DSSContextRequestRef<ImportWitContextAndStreamRequestRefImpl>{
        @Override
        public boolean isLinkisBMLResources() {
            return false;
        }
    }

    class UpdateRequestRefImpl extends DevelopmentRequestRefImpl<UpdateRequestRefImpl>
            implements UpdateRequestRef<UpdateRequestRefImpl>, ProjectRefRequestRef<UpdateRequestRefImpl> {}

    class UpdateWitContextRequestRefImpl extends DevelopmentRequestRefImpl<UpdateWitContextRequestRefImpl>
            implements UpdateRequestRef<UpdateWitContextRequestRefImpl>, ProjectRefRequestRef<UpdateWitContextRequestRefImpl>,
            DSSContextRequestRef<UpdateWitContextRequestRefImpl>{}

    class QueryJumpUrlRequestRefImpl extends DevelopmentRequestRefImpl<QueryJumpUrlRequestRefImpl>
            implements QueryJumpUrlRequestRef<QueryJumpUrlRequestRefImpl>, ProjectRefRequestRef<QueryJumpUrlRequestRefImpl> {}

    class QueryJumpUrlWithContextRequestRefImpl extends DevelopmentRequestRefImpl<QueryJumpUrlWithContextRequestRefImpl>
            implements QueryJumpUrlRequestRef<QueryJumpUrlWithContextRequestRefImpl>, ProjectRefRequestRef<QueryJumpUrlWithContextRequestRefImpl>,
            DSSContextRequestRef<QueryJumpUrlWithContextRequestRefImpl> {}

    class QueryJumpUrlWithDSSJobContentRequestRefImpl extends DevelopmentRequestRefImpl<QueryJumpUrlWithDSSJobContentRequestRefImpl>
            implements QueryJumpUrlRequestRef<QueryJumpUrlWithDSSJobContentRequestRefImpl>, ProjectRefRequestRef<QueryJumpUrlWithDSSJobContentRequestRefImpl>,
            DSSJobContentRequestRef<QueryJumpUrlWithDSSJobContentRequestRefImpl>{}

    class QueryJumpUrlWithContextAndDSSJobContentRequestRefImpl extends DevelopmentRequestRefImpl<QueryJumpUrlWithContextAndDSSJobContentRequestRefImpl>
            implements QueryJumpUrlRequestRef<QueryJumpUrlWithContextAndDSSJobContentRequestRefImpl>, ProjectRefRequestRef<QueryJumpUrlWithContextAndDSSJobContentRequestRefImpl>,
            DSSContextRequestRef<QueryJumpUrlWithContextAndDSSJobContentRequestRefImpl>, DSSJobContentRequestRef<QueryJumpUrlWithContextAndDSSJobContentRequestRefImpl> {}
}
