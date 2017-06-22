package com.joysee.adtv.aidl.email;

import java.util.List;
import java.util.Map;
import com.joysee.adtv.logic.bean.EmailHead;

interface IEmailService {
    /**
     * 查看邮件空间
     * @return 可用空间
     */
    int getEmailIdleSpace();
    
    /**
     * 得到当前邮件数量
     * @return 当前邮件总数
     */
    int getEmailUsedSpace();

    /**
     * 获取邮件头列表.
     * @return 邮件头列表
     */
     List<EmailHead> getEmailHeads();

    /**
     * @param id 邮件ID
     * @return content 邮件内容
     */
     String getEmailContent(in int id);

    /**
     * @param id 删除邮件
     * @return >=0成功
     */
    int DelEmailByID(int id);
}
