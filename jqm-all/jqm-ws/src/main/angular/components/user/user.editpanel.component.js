'use strict';

import template from './user.editpanel.template.html';
import 'bootstrap-datepicker/dist/js/bootstrap-datepicker'
import 'bootstrap-datepicker/dist/css/bootstrap-datepicker.min.css'

class UserEditPanelCtrl
{
    // From bindings: user, roles.
    constructor()
    {
        this.selected_user = null;

        angular.element(this.afterDomReady.bind(this));
    }

    afterDomReady()
    {
        /* angular.element(document.getElementById("dtExp")).datepicker().on('changeDate', function (e)
         {
             alert("tttt");
         });*/
        console.debug(this);
    }

    test()
    {
        alert("mmmmmmmm");
    }
}

export const userEditPanelComponent = {
    controller: UserEditPanelCtrl,
    template: template,
    bindings: {
        user: "=",
        roles: "<",
    }
};
