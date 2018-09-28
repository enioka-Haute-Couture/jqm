'use strict';

import $ from 'jquery';
import 'bootstrap/js/dist/popover';


class HelpTagCtrl
{
    $onInit()
    {
        $('[data-toggle="popover"]').popover();
    }
}


export const helpTagComponent = {
    controller: HelpTagCtrl,
    template: '<span data-toggle="popover" data-content="Click to display help" data-trigger="hover" data-placement="right" class="fas fa-question-circle"></span>',
    bindings: {}
};
