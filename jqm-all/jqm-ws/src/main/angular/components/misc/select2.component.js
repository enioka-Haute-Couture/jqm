'use strict';

import $ from 'jquery';
import 'select2/dist/js/select2.full';
import 'select2/dist/css/select2.min.css';


class Select2Controller
{
    constructor($scope)
    {
        this.$scope = $scope;
        this.parentId = ("parent-" + Math.random()).replace('.', '');
        this.selectId = ("select2-" + Math.random()).replace('.', '');
        angular.element(this.afterDomReady.bind(this));
    }

    $onInit()
    {
        this.display = this.displayKey || 'name';
        this.pk = this.idKey || "id";
    }

    $onChanges(changesObj)
    {
        if (changesObj.data)
        {
            if (this.data.$promise && !this.data.$resolved)
            {
                this.data.$promise.then(this.refreshData.bind(this));
            }
            else
            {
                this.refreshData();
            }
        }
    }

    refreshData()
    {
        var $ctrl = this;

        if (this.data)
        {
            // Index data
            this.allData = {};
            $.each(this.data, function () { $ctrl.allData[this[$ctrl.pk]] = this; });

            // Create S2-compatible data
            this.data2 = [];
            $.each(this.data, function () { $ctrl.data2.push({ id: this[$ctrl.pk], text: this[$ctrl.display] }); });
        }

        if (this.jqCompo)
        {
            this.jqCompo.select2({ data: this.data2 });
        }
    }

    afterDomReady()
    {
        var $ctrl = this;
        this.jqCompo = $("#" + this.selectId);
        this.jqCompo.select2({
            allowClear: this.clearable,
            multiple: false,
            width: "resolve",
            dropdownParent: $("#" + this.parentId),
            data: this.data2 || [],
        });

        this.jqCompo.on('change', function (e)
        {
            // Set the binding field to the initial model.
            var selected = $ctrl.jqCompo.select2('data')[0];
            if (!selected || selected.length === 0)
            {
                $ctrl.ngModel = null;
            }
            else
            {
                $ctrl.ngModel = $ctrl.allData[selected.id];
            }

            // Let NG refresh its internals...
            $ctrl.$scope.$applyAsync();

            // Call optional callback.
            if ($ctrl.onChange)
            {
                $ctrl.onChange(e);
            }
        });

    }

    $onDestroy()
    {
        // Remove close dialog handler - avoids leaks.
        this.jqCompo.off('change');
    }
}
Select2Controller.$inject = ['$scope',];


export const select2Component = {
    controller: Select2Controller,
    template: '<div id="{{$ctrl.parentId}}"><select id="{{$ctrl.selectId}}" data-placeholder="{{$ctrl.placeholder}}"><option></option></select></div>',
    bindings: {
        'data': '<',
        'ngModel': '=',
        'displayKey': '@',
        'idKey': '@',
        'placeholder': '@',
        'clearable': '@',
        'onChange': '<?',
    }
};
