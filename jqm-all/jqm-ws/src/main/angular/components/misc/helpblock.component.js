'use strict';

import template from './helpblock.template.html';


class HelpBlockCtrl
{ }

export const helpBlockComponent = {
    controller: HelpBlockCtrl,
    template: template,
    bindings: {
        content: "<"
    }
};
