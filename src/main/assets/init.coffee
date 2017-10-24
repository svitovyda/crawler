'use strict'

requirejs ["angular", "angular-resource"], ->
  require ["angular"]
  angular = window.angular
  app = angular.module 'Application', ["ngResource"]

  app.controller 'main', ($scope, $resource)->

    $scope.count = 0

    $scope.addModel =
      url: ""

    $scope.searchModel =
      query: ""

    $scope.showModel =
      result: []

    $scope.addUrl = () ->
      $http
        url: "/pages"
        method: "POST"
        data: JSON.stringify $scope.add
      .then ({data:resp})->
        $scope.count = resp.count

    $scope.searchText = () ->
      $http
        url: "/pages/search"
        method: "GET"
        data: JSON.stringify $scope.search
      .then ({data:resp})->
        $scope.show.result = resp.list


    counter = $resource "/pages/count"
    counter.get().$promise.then (resp)->
      console.log("counter: " + resp)
      $scope.count = resp.count
