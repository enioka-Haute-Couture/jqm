'use strict';

import template from './filereader.template.html';


class FileReaderController
{
    constructor($http)
    {
        this.data = null;
        this.$http = $http;
    }

    $doCheck()
    {
        if (this.previousShow !== this.show && this.href)
        {
            this.getData();
        }
        this.previousShow = this.show;
    }

    getData()
    {
        this.$http.get(this.href).then(this.getDataOk.bind(this), this.getDataKo.bind(this));
    };

    getDataOk(data)
    {
        this.data = data.data;
    };

    getDataKo()
    {
        this.show = false;
    };
};
FileReaderController.$inject = ['$http',];

export const FileReaderComponent = {
    controller: FileReaderController,
    template: template,
    bindings: {
        show: '=',
        href: '<'
    }
};
