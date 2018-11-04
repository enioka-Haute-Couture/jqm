'use strict';

import angular from 'angular';
import ngRoute from 'angular-route';
import ngSanitize from 'angular-sanitize';

import jqmComponentsModule from './components';

import './css/app.css';


var jqmApp = angular.module('jqmApp', [jqmComponentsModule, ngRoute, ngSanitize]);

jqmApp.config(['$routeProvider', '$locationProvider', function ($routeProvider, $locationProvider)
{
    $locationProvider.hashPrefix('!');

    $routeProvider.when('/', {
        template: '<jqm-home></jqm-home>'
    }).when('/home', {
        template: '<jqm-home></jqm-home>'
    }).when('/node', {
        template: '<jqm-nodes></jqm-nodes>'
    }).when('/q', {
        template: '<jqm-queues></jqm-queues>'
    }).when('/jndi', {
        template: '<jqm-jndi></jqm-jndi>'
    }).when('/prm', {
        template: '<jqm-prms></jqm-prms>'
    }).when('/jd', {
        template: '<jqm-jds2></jqm-jds2>'
    }).when('/user', {
        template: '<jqm-users></jqm-users>'
    }).when('/role', {
        template: '<jqm-roles></jqm-roles>'
    }).when('/history', {
        template: '<jqm-history></jqm-history>'
    }).when('/qmapping', {
        template: '<jqm-mappings></jqm-mappings>'
    }).otherwise({
        redirectTo: '/'
    });
}
]);
