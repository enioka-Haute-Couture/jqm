'use strict';

var jqmServices = angular.module('jqmServices', [ 'ngResource' ]);

jqmServices.factory('µNodeDto', [ '$resource', function($resource)
{
    return $resource('ws/admin/node/:id', {}, {
        saveAll : {
            method : 'PUT',
            isArray : true
        },
    });
} ]);

jqmServices.factory('µQueueDto', function($resource)
{
    return $resource('ws/admin/q/:id', {
        id : ''
    }, {
        query : {
            method : 'GET',
            isArray : true
        },
        saveAll : {
            method : 'PUT',
            isArray : true
        },

        /*
         * remove : { method : 'DELETE' },
         */
        save : {
            method : 'POST'
        },
    });
});

jqmServices.factory('µQueueMappingDto', function($resource)
{
    return $resource('ws/admin/qmapping/:id', {
        id : ''
    }, {
        saveAll : {
            method : 'PUT',
            isArray : true
        },
    });
});

jqmServices.factory('µJndiDto', function($resource)
{
    return $resource('ws/admin/jndi/:id', {
        id : ''
    });
});

jqmServices.factory('µPrmDto', function($resource)
{
    return $resource('ws/admin/prm/:id', {
        id : ''
    }, {
        saveAll : {
            method : 'PUT',
            isArray : true
        },
    });
});

jqmServices.factory('µJdDto', function($resource)
{
    return $resource('ws/admin/jd/:id', {
        id : ''
    }, {
        saveAll : {
            method : 'PUT',
            isArray : true
        },
    });
});

jqmServices.factory('µUserDto', function($resource)
{
    return $resource('ws/admin/user/:id', {
        id : ''
    }, {
        saveAll : {
            method : 'PUT',
            isArray : true
        },
    });
});

jqmServices.factory('µRoleDto', function($resource)
{
    return $resource('ws/admin/role/:id', {
        id : ''
    }, {
        saveAll : {
            method : 'PUT',
            isArray : true
        },
    });
});

jqmServices.factory('µUserPerms', function($resource)
{
    return $resource('ws/admin/me', {}, {
        query : {
            method : 'GET',
            isArray : false
        },
    });
});

jqmServices.factory('µUserJdDto', function($resource)
{
    return $resource('ws/client/jd/:id', {
        id : ''
    });
});