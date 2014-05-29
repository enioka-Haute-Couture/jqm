var jqmServices = angular.module('jqmServices', [ 'ngResource' ]);

jqmServices.factory('µNodeService', [ '$resource', function($resource)
{
    return $resource('admin/node/:nodeId', {}, {
        query : {
            method : 'GET',
            params : {
                nodeId : ''
            },
            isArray : true
        }
    });
} ]);

jqmServices.factory('µQueueDto', function($resource)
{
    return $resource('admin/q/:qId', {
        qId : ''
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
    return $resource('admin/qmapping/:mId', {
        mId : ''
    }, {
        saveAll : {
            method : 'PUT',
            isArray : true
        },
    });
});

jqmServices.factory('µJndiDto', function($resource)
{
    return $resource('admin/jndi/:mId', {
        mId : ''
    });
});

jqmServices.factory('µPrmDto', function($resource)
{
    return $resource('admin/prm/:pId', {
        pId : ''
    }, {
        saveAll : {
            method : 'PUT',
            isArray : true
        },
    });
});