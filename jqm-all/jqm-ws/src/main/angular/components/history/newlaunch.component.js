'use strict';

import template from './newlaunch.template.html';


class NewLaunchController
{
    constructor($http, µUserJdDto, µPermManager)
    {
        this.jds = µUserJdDto.query();
        this.selectedJd = null;
        this.$http = $http;
        this.me = µPermManager;

        this.data = {
            selectedJd: null
        };

        var $ctrl = this;
        this.request = {
            user: null,
            sessionID: 0,
            parameters: [],
            startState: 'SUBMITTED',
            module: "JQM web UI"
        };
        µPermManager.perms.$promise.then(function () { $ctrl.request.user = µPermManager.perms.enforced ? µPermManager.perms.login : 'webuser'; });
    }

    addPrm()
    {
        this.request.parameters.push({ key: "", value: "" });
    };


    removePrm(p)
    {
        this.request.parameters = this.request.parameters.filter(function (x) { return x !== p; });
    };

    canAdd(p)
    {
        return this.request.parameters.every(function (x)
        {
            return x.key && x.value;
        });
    };

    postOk(response)
    {
        this.show = false;
        if (this.onLaunched)
        {
            this.onLaunched(response.data);
        }
    };

    ok()
    {
        this.request.applicationName = this.selectedJd.applicationName;
        this.$http.post("ws/client/ji", this.request).then(this.postOk.bind(this));
    };

    cancel()
    {
        this.show = false;
    };
}
NewLaunchController.$inject = ['$http', 'µUserJdDto', 'µPermManager'];



export const newLaunchComponent = {
    controller: NewLaunchController,
    template: template,
    bindings: {
        'show': '=',
        'onLaunched': '<',
    }
};
