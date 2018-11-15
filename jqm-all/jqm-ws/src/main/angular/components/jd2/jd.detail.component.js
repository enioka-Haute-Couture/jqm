'use strict';

import template from './jd.detail.template.html';

class JdDetailCtrl
{
    constructor()
    {

    }
}


export const jdDetailComponent = {
    controller: JdDetailCtrl,
    template: template,
    bindings: {
        'jd': '=',
        'onRemove': '<?',
    }
};
