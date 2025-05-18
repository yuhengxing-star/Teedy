'use strict';

/**
 * Document view activity controller.
 */
angular.module('docs').controller('DocumentViewActivity', function ($scope, $stateParams, Restangular, $http) {
  // Load audit log data from server
  Restangular.one('auditlog').get({
    document: $stateParams.id
  }).then(function(data) {
    $scope.logs = data.logs;
  });

  $scope.translatedDescription = null;
  $scope.translating = false;
  $scope.translateError = null;

  $scope.translatedTxt = null;
  $scope.translatingTxt = false;
  $scope.translateTxtError = null;

  // 语言映射表（前端->百度API）
  var langMap = {
    'zh-CN': 'zh',
    'en': 'en',
    'fr': 'fra',
    'de': 'de',
    'ja': 'jp'
  };

  $scope.translateDocument = function(lang) {
    $scope.translating = true;
    $scope.translateError = null;
    var baiduLang = langMap[lang] || lang;
    Restangular.one('document', $stateParams.id).all('translate').post({ lang: baiduLang })
      .then(function(resp) {
        $scope.translatedDescription = resp.translated_description;
        $scope.translating = false;
      }, function(err) {
        $scope.translateError = err.data && err.data.message ? err.data.message : '翻译失败';
        $scope.translating = false;
      });
  };

  $scope.translateTxtFile = function() {
    alert('方法已触发');
    var fileInput = document.getElementById('txtFile');
    if (!fileInput.files.length) {
      $scope.translateTxtError = '请选择txt文件';
      return;
    }
    var formData = new FormData();
    formData.append('file', fileInput.files[0]);
    formData.append('lang', $scope.selectedLang || 'en');
    $scope.translatingTxt = true;
    $scope.translateTxtError = null;
    $http.post('../api/document/translate_txt', formData, {
      headers: { 'Content-Type': undefined }
    }).then(function(resp) {
      $scope.translatedTxt = resp.data.translated_text;
      $scope.translatingTxt = false;
    }, function(err) {
      $scope.translatedTxt = null;
      $scope.translateTxtError = '翻译失败: ' + (err.data && err.data.message ? err.data.message : '');
      $scope.translatingTxt = false;
    });
  };
});