var jqmServices = angular.module('jqmServices', [ 'ngResource' ]);

jqmServices.factory('µNodeDto', [ '$resource', function($resource)
{
    return $resource('admin/node/:id', {}, {
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
    return $resource('admin/q/:id', {
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
    return $resource('admin/qmapping/:id', {
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
    return $resource('admin/jndi/:id', {
        id : ''
    });
});

jqmServices.factory('µPrmDto', function($resource)
{
    return $resource('admin/prm/:id', {
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
    return $resource('admin/jd/:id', {
        id : ''
    }, {
        saveAll : {
            method : 'PUT',
            isArray : true
        },
    });
});