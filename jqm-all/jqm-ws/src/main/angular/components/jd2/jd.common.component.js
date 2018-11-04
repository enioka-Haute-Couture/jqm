'use strict';

import template from './jd.common.template.html';

class JdCommonCtrl
{
    constructor(µQueueDto)
    {
        this.queues = µQueueDto.query();
    }
}
JdCommonCtrl.$inject = ["µQueueDto"];

export const jdCommonComponent = {
    controller: JdCommonCtrl,
    template: template,
    bindings: {
        "jd": '='
    }
};
