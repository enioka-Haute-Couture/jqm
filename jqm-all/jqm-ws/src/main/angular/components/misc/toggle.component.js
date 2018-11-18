'use strict'

import $ from 'jquery'; // needed by bootstrap js.
import 'bootstrap/js/dist/button';

class ToggleController
{
    constructor($timeout)
    {
        this.$timeout = $timeout;
        this.ngItemClass = "btn btn-light"
        this.name = ("radio-" + Math.random()).replace('.', '');

        angular.element(this.afterDomReady.bind(this));
    }

    afterDomReady()
    {
        this.refresh();
    }

    $onChanges(changesObj)
    {
        if (changesObj.ngData)
        {
            this.refresh();
        }
    }

    $doCheck()
    {
        if (this.previousValue !== this.ngValue)
        {
            this.refresh();
            this.previousValue = this.ngValue;
        }
    }

    refresh()
    {
        if (!this.ngValue)
        {
            return;
        }

        var $ctrl = this;
        $("#" + this.name + " label").each(function ()
        {
            var c = $(this);
            if (c.data("value") === $ctrl.ngValue)
            {
                c.addClass("active");
            }
            else
            {
                c.removeClass("active");
            }
        });

    }

    onChange(item)
    {
        if (item === this.ngValue)
        {
            return;
        }

        this.ngValue = item;
        if (this.valueChange)
        {
            var $ctrl = this;
            this.$timeout(function ()
            {
                $ctrl.valueChange.bind($ctrl)(item);
            });
        }
    }
}
ToggleController.$inject = ["$timeout",];


export const toggleComponent = {
    controller: ToggleController,
    template: '' +
        '<div id="{{$ctrl.name}}" class="btn-group btn-group-toggle" data-toggle="buttons"> ' +
        '    <label ng-repeat="item in $ctrl.ngData" ng-class="$ctrl.ngItemClass" data-value="{{item[0]}}" ng-click="$ctrl.onChange(item[0])" > ' +
        '        <input type="radio" name="{{$ctrl.name}}" value="{{item[0]}}" autocomplete="off"> ' +
        '        <span class="{{item[2]}}"></span> ' +
        '        {{item[1]}} ' +
        '    </label>' +
        '</div>',
    bindings: {
        'ngData': '<',
        'ngItemClass': '<?',
        'ngValue': '=',
        'valueChange': '<?',
    }
};
