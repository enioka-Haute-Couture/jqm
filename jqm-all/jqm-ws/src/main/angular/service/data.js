'use strict';

import angular from 'angular';
import ngResource from 'angular-resource';

var jqmServices = angular.module('jqmServices', [ngResource]);
export default jqmServices.name;

jqmServices.factory('µNodeDto', ['$resource', function ($resource)
{
    return $resource('ws/admin/node/:id', {}, {
        saveAll: {
            method: 'PUT',
            isArray: true
        },
    });
}]);

jqmServices.factory('µQueueDto', ['$resource', function ($resource)
{
    return $resource('ws/admin/q/:id', {
        id: ''
    }, {
            query: {
                method: 'GET',
                isArray: true
            },
            saveAll: {
                method: 'PUT',
                isArray: true
            },

            /*
             * remove : { method : 'DELETE' },
             */
            save: {
                method: 'POST'
            },
        });
}]);

jqmServices.factory('µQueueMappingDto', ['$resource', function ($resource)
{
    return $resource('ws/admin/qmapping/:id', {
        id: ''
    }, {
            saveAll: {
                method: 'PUT',
                isArray: true
            },
        });
}]);

jqmServices.factory('µJndiDto', ['$resource', function ($resource)
{
    return $resource('ws/admin/jndi/:id', {
        id: ''
    });
}]);

jqmServices.factory('µPrmDto', ['$resource', function ($resource)
{
    return $resource('ws/admin/prm/:id', {
        id: ''
    }, {
            saveAll: {
                method: 'PUT',
                isArray: true
            },
        });
}]);

jqmServices.factory('µJdDto', ['$resource', function ($resource)
{
    return $resource('ws/admin/jd/:id', {
        id: ''
    }, {
            saveAll: {
                method: 'PUT',
                isArray: true
            },
        });
}]);

jqmServices.factory('µUserDto', ['$resource', function ($resource)
{
    return $resource('ws/admin/user/:id', {
        id: ''
    }, {
            saveAll: {
                method: 'PUT',
                isArray: true
            },
        });
}]);

jqmServices.factory('µRoleDto', ['$resource', function ($resource)
{
    return $resource('ws/admin/role/:id', {
        id: ''
    }, {
            saveAll: {
                method: 'PUT',
                isArray: true
            },
        });
}]);

jqmServices.factory('µUserPerms', ['$resource', function ($resource)
{
    return $resource('ws/admin/me', {}, {
        query: {
            method: 'GET',
            isArray: false
        },
    });
}]);

// A cache for the permission data.
jqmServices.service('µPermManager', ['µUserPerms', function (µUserPerms)
{
    this.perms = µUserPerms.get(null);
}]);

jqmServices.factory('µUserJdDto', ['$resource', function ($resource)
{
    return $resource('ws/client/jd/:id', {
        id: ''
    });
}]);
