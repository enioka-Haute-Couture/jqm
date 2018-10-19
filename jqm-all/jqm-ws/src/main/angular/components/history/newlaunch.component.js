'use strict';

import template from './newlaunch.template.html';


class NewLaunchController
{
    constructor($http, µUserJdDto)
    {
        this.jds = µUserJdDto.query();
        this.selectedJd = null;
        this.$http = $http;

        this.data = {
            selectedJd: null,
            newKey: null,
            newValue: null
        };

        this.request = {
            user: 'webuser',
            sessionID: 0,
            parameters: [],
        };
    }

    addPrm()
    {
        var np = {};
        np.key = this.data.newKey;
        np.value = this.data.newValue;
        this.request.parameters.push(np);
        this.data.newKey = null;
        this.data.newValue = null;
    };

    postOk()
    {
        this.show = false;
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
NewLaunchController.$inject = ['$http', 'µUserJdDto'];



export const newLaunchComponent = {
    controller: NewLaunchController,
    template: template,
    bindings: {
        'show': '=',
    }
};
