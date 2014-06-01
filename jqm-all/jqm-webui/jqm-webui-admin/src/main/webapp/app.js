'use strict';

var jqmApp = angular.module('jqmApp', [ 'ngRoute', 'ngCookies', 'jqmControllers', 'ngGrid', 'jqmServices', 'ui.bootstrap', ]);

jqmApp.config([ '$routeProvider', function($routeProvider)
{
    $routeProvider.when('/home', {
        templateUrl : 'template/home.html',
        controller : 'µHomeController'
    }).when('/node', {
        templateUrl : 'template/node_list.html',
        controller : 'µNodeListCtrl'
    }).when('/node/:nodeId', {
        templateUrl : 'template/node_detail.html',
        controller : 'µNodeDetailCtrl'
    }).when('/q', {
        templateUrl : 'template/q_list.html',
        controller : 'µQueueListCtrl'
    }).when('/qmapping', {
        templateUrl : 'template/mapping_list.html',
        controller : 'µQueueMappingListCtrl'
    }).when('/jndi', {
        templateUrl : 'template/jndi_list.html',
        controller : 'µJndiListCtrl'
    }).when('/prm', {
        templateUrl : 'template/prm_list.html',
        controller : 'µPrmListCtrl'
    }).when('/jd', {
        templateUrl : 'template/jd_list.html',
        controller : 'µJdListCtrl'
    }).when('/user', {
        templateUrl : 'template/user_list.html',
        controller : 'µUserListCtrl'
    }).when('/role', {
        templateUrl : 'template/role_list.html',
        controller : 'µRoleListCtrl'
    }).otherwise({
        redirectTo : '/home'
    });
} ]);

function TabsCtrl($scope, $location, $http)
{
    $http.defaults.headers.common['X-Requested-With'] = 'XMLHttpRequest';

    $scope.tabs = [ {
        link : '#/home',
        label : 'Home',
        permission : '',
    }, {
        link : '#/node',
        label : 'Nodes',
        permission : 'node:read',
    }, {
        link : '#/q',
        label : 'Queues',
        permission : 'queue:read',
    }, {
        link : '#/qmapping',
        label : 'Queue Mappings',
        permission : 'qmapping:read',
    }, {
        link : '#/jndi',
        label : 'JNDI Resources',
        permission : 'jndi:read',
    }, {
        link : '#/prm',
        label : 'Cluster-wide parameters',
        permission : 'prm:read',
    }, {
        link : '#/jd',
        label : 'Job definitions',
        permission : 'jd:read',
    }, {
        link : '#/user',
        label : 'Users',
        permission : 'user:read',
    }, {
        link : '#/role',
        label : 'Roles',
        permission : 'role:read',
    }, ];

    $scope.selectedTab = $scope.tabs[0];
    var i = 0;
    for (; i < $scope.tabs.length; i++)
    {
        if ($scope.tabs[i].link === "#" + $location.path())
        {
            $scope.selectedTab = $scope.tabs[i];
            break;
        }
    }

    $scope.setSelectedTab = function(tab)
    {
        $scope.selectedTab = tab;
    };

    $scope.tabClass = function(tab)
    {
        if ($scope.selectedTab == tab)
        {
            return "active";
        }
        else
        {
            return "";
        }
    };
}

Date.prototype.addDays = function(days)
{
    var dat = new Date(this.valueOf());
    dat.setDate(dat.getDate() + days);
    return dat;
};

// /////////////////////////////////////////////////////////////////////
// Login & permissions handling
// ///////////////////////////////////////////////////////////////////

jqmApp.service('µPermManager', function(µUserPerms, $http, $cookieStore, $rootScope, httpBuffer)
{
    this.perms = [];
    var scope = this;

    this.init = function(login, password)
    {
        $http.defaults.headers.common['Authorization'] = "Basic " + btoa(login + ":" + password);
        scope.perms = µUserPerms.get(null, this.permsok, this.permsko);
    };

    this.permsok = function(value, responseHeaders)
    {
        var updater = function(config)
        {
            return config;
        };
        $rootScope.$broadcast('event:auth-loginConfirmed');
        httpBuffer.retryAll(updater);
    };

    this.permsko = function(httpResponse)
    {
        // console.debug(httpResponse);
    };

    this.logout = function()
    {
        delete $http.defaults.headers.common['Authorization'];
        scope.perms = [];
    };

    this.loginCancelled = function(data, reason)
    {
        httpBuffer.rejectAll(reason);
        $rootScope.$broadcast('event:auth-loginCancelled', data);
    };

});

jqmApp.config(function($httpProvider)
{
    $httpProvider.interceptors.push(function($q, $rootScope, httpBuffer)
    {
        return {
            'responseError' : function(rejection)
            {
                if (rejection.status === 401)
                {
                    var deferred = $q.defer();
                    httpBuffer.append(rejection.config, deferred);
                    $rootScope.$broadcast('event:auth-loginRequired', rejection);
                    return deferred.promise;
                }
                return $q.reject(rejection);
            },
        };
    });
});

jqmApp.controller('µCredentialsController', function($scope, µPermManager, $location)
{
    $scope.data = {
        login : null,
        password : null,
    };

    $scope.logmeout = function()
    {
        µPermManager.logout();
        window.location = './out.html';
    };

    $scope.logmein = function()
    {
        µPermManager.init($scope.data.login, $scope.data.password);
    };
});

jqmApp.directive('loginDialog', function($modal)
{
    return {
        // templateUrl : './template/login.html',
        template : '<button ng-click="logmeout()">Logout</button>',
        restrict : 'E',
        replace : true,
        controller : 'µCredentialsController',
        link : function(scope, element, attributes, controller)
        {
            var a = null;
            scope.$on('event:auth-loginRequired', function()
            {
                if (a === null)
                {
                    a = $modal.open({
                        templateUrl : './template/login.html',
                        controller : 'µCredentialsController',
                        size : 650,
                    });
                }
            });

            scope.$on('event:auth-loginConfirmed', function()
            {
                a.close();
                a = null;
            });
        }
    };
});

jqmApp.factory('httpBuffer', [ '$injector', function($injector)
{
    /** Holds all the requests, so they can be re-requested in future. */
    var buffer = [];

    /** Service initialized later because of circular dependency problem. */
    var $http = null;

    function retryHttpRequest(config, deferred)
    {
        delete config.headers['Authorization'];

        function successCallback(response)
        {
            deferred.resolve(response);
        }
        function errorCallback(response)
        {
            deferred.reject(response);
        }
        $http = $http || $injector.get('$http');
        $http(config).then(successCallback, errorCallback);
    }

    return {
        /**
         * Appends HTTP request configuration object with deferred response attached to buffer.
         */
        append : function(config, deferred)
        {
            buffer.push({
                config : config,
                deferred : deferred
            });
        },

        /**
         * Abandon or reject (if reason provided) all the buffered requests.
         */
        rejectAll : function(reason)
        {
            if (reason)
            {
                for ( var i = 0; i < buffer.length; ++i)
                {
                    buffer[i].deferred.reject(reason);
                }
            }
            buffer = [];
        },

        /**
         * Retries all the buffered requests clears the buffer.
         */
        retryAll : function(updater)
        {
            for ( var i = 0; i < buffer.length; ++i)
            {
                retryHttpRequest(updater(buffer[i].config), buffer[i].deferred);
            }
            buffer = [];
        }
    };
} ]);

jqmApp.directive('jqmPermission', function(µPermManager)
{
    return {
        restrict : 'A',
        replace : false,
        scope : {
            jqmPermission : "@",
        },
        link : function(scope, element, attrs)
        {
            var perms = µPermManager.perms;

            var shouldDisplay = function()
            {
                // If the control does not ask for a particular permission, always enable it.
                if (scope.jqmPermission === "" || scope.jqmPermission === undefined)
                {
                    console.debug("PPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP");
                    return true;
                }

                // If the user has no permission at all, disable the control
                perms = µPermManager.perms.permissions;
                if (perms == undefined || perms.length == 0)
                {
                    return false;
                }

                // The permission to look for (requested permission => r_)
                var r_noun = scope.jqmPermission.split(":")[0];
                var r_verb = scope.jqmPermission.split(":")[1];

                var i = 0;
                var found = false;
                for (i = 0; i < perms.length; i++)
                {
                    var p = perms[i];
                    var noun = p.split(":")[0];
                    var verb = p.split(":")[1];

                    if ((r_noun == noun || noun == "*") && (r_verb == verb || verb == "*"))
                    {
                        found = true;
                        break;
                    }
                }
                return found;
            };

            var updatePerm = function()
            {
                if (!shouldDisplay())
                {
                    if (element.prop("tagName") === "BUTTON")
                    {
                        element.prop('disabled', true);
                    }
                    else
                    {
                        element.hide();
                    }
                }
                else
                {
                    element.show();
                    element.prop('disabled', false);
                }
            };

            scope.$on('event:auth-loginConfirmed', function()
            {
                updatePerm();
            });

            // Permission is in the for : object:verb
            updatePerm();
        }
    };
});

jqmApp.controller('µHomeController', function($scope, µUserPerms, µPermManager)
{
    $scope.login = function()
    {
        µPermManager.logout();
        µUserPerms.get();
    };
});