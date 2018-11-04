'use strict';

import template from './home.template.html';


class HomeController
{
    constructor(µPermManager)
    {
        this.me = µPermManager;
    }
};
HomeController.$inject = ['µPermManager',];


export const homeComponent = {
    controller: HomeController,
    template: template,
    bindings: {}
};
