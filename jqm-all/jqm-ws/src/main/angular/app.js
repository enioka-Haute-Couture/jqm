'use strict';

import angular from 'angular';
import ngRoute from 'angular-route';
import ngSanitize from 'angular-sanitize';

import jqmComponentsModule from './components';

import './css/app.css';


var jqmApp = angular.module('jqmApp', [jqmComponentsModule, ngRoute, ngSanitize]);

jqmApp.config(['$routeProvider', '$locationProvider', function ($routeProvider, $locationProvider) {
  $locationProvider.hashPrefix('!');

  $routeProvider.when('/', {
    template: '<home></home>'
  }).when('/home', {
    template: '<home></home>'
  }).when('/node', {
    template: '<nodes></nodes>'
  }).when('/q', {
    template: '<queues></queues>'
  }).when('/jndi', {
    template: '<jndi></jndi>'
  }).when('/prm', {
    template: '<prms></prms>'
  }).when('/jd', {
    template: '<jds></jds>'
  }).when('/user', {
    template: '<users></users>'
  }).when('/role', {
    template: '<roles></roles>'
  }).otherwise({
    redirectTo: '/'
  });
}
]);
