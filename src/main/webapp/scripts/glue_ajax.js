    var url = "/dam/damsvc";
     
    function doIt(action, otherParamsStr)
    {
      paramStr = "";
      if(action.length > 0) paramStr = "action="+action;
      if(otherParamsStr.length > 0) paramStr += "&";
      paramStr += otherParamsStr;
      
      var target = 'output';
      $('call').value = url + ((paramStr && paramStr.length > 0) ? "?" + paramStr : '');
      $('output').value = 'Waiting for response ....';
      //console.log("after setting value of field with call id to ", $('call').value)
      //console.log(paramStr);
      //var myAjax = new Ajax.Updater(target, url, {method: 'post', parameters: paramStr});
      new Ajax.Request(url,
        {
          method:'post',
          parameters: paramStr,
          onSuccess: function(transport){
            var response = transport.responseText || "no response text";
            $('output').value = response;
          },
          onFailure: function(){ alert('Something went wrong...'); }
        });
      return false;
    }
    
    function doPostWithXmlFromTextarea(url, paramName, alertIfResponseNotTheSame) 
    {
      url = "adbuilder/" + url;
      if (typeof alertIfResponseNotTheSame == 'undefined' )
         alertIfResponseNotTheSame = false;
      param = paramName + "=" + $('output').value
      //console.log("PARAM WILL SEND: ", param)
      $('output').value = 'Waiting for response ....'
      $('call').value = url + "?" + paramName + "= ...text from output area ...";
      // note: using AjaxUpdater didn't work if textarea's value had been changed .. it didn't update it 
      new Ajax.Request(url,
        {
          method:'post',
          parameters: param,
          onSuccess: function(transport){
            var response = transport.responseText || "no response text";
            $('output').value = response
            if(alertIfResponseNotTheSame && ( (paramName + "=" + transport.responseText).trim() != param ))
              alert("Expected response to be same as sent but it was not!") 
          },
          onFailure: function(){ alert('Something went wrong...') }
        });    
    }
    
        
    function makeCallFromTextField(txtValue)
    {
      var posOfQMark = txtValue.indexOf("?");
      var params = (posOfQMark == -1) ? '' : txtValue.substring(posOfQMark + 1);
      //console.log("url: ", url)
      //console.log("params: ", params)
      $('output').value = 'Waiting for response ....'
      // note: using AjaxUpdater didn't work if textarea's value had been changed .. it didn't update it 
      new Ajax.Request(url,
        {
          method:'post',
          parameters: params,
          onSuccess: function(transport){
            var response = transport.responseText || "no response text";
            $('output').value = response
          },
          onFailure: function(){ alert('Something went wrong...') }
        });    
    }
    
    function doZipFileCall(f)
    {
      var zipFileName = f.zipFileName.value;
      var zipFolderIds = f.zipFolderIds.value;
      var zipAssetIds = f.zipAssetIds.value;
      var params = "name="+zipFileName+"&folderids="+zipFolderIds+"&assetids="+zipAssetIds;
      doIt('zipFile',params)
      return false;
    }
    
    function doAddAssetTagCall(f)
    {
      var assetName = f.addAssetName.value;
      var assetTag = f.addAssetTag.value;
      var assetValue = f.addAssetValue.value;
      var params = "name="+assetName+"&tag="+assetTag+"&value="+assetValue;
      doIt('addAssetTag',params)
      return false;
    }
    
    function doDeleteAssetTagCall(f)
    {
      var assetName = f.deleteAssetName.value;
      var assetTag = f.deleteAssetTag.value;
      var params = "name="+assetName+"&tag="+assetTag;
      doIt('deleteAssetTag',params)
      return false;
    }
    
    function doCreateUserFolderCall(f)
    {
      var folderName = f.createUserFolderName.value;
      var params = "name="+folderName;
      doIt('createUserFolder',params)
      return false;
    }   
    
    function doMoveToFolderCall(f)
    {
      var moveFolderId = f.moveFolderId.value;
      var moveToFolderId = f.moveToFolderId.value;
      var params = "id="+moveFolderId+"&toid="+moveToFolderId;
      doIt('moveFolder',params)
      return false;
    }
    
    function doRenameFolderCall(f)
    {
      var renameFolderId = f.renameFolderId.value;
      var renameToFolder = f.renameToFolder.value;
      var params = "id="+renameFolderId+"&toname="+renameToFolder;
      doIt('renameUserFolder',params)
      return false;
    }
    
    function doChangeToFolderCall(f)
    {
      var folderId = f.changeToFolderId.value;
      var params = null;
      params = "id="+folderId;
      doIt('changeToFolder',params)
      return false;
    }   
    
    function doDeleteFolderCall(f)
    {
      var folderId = f.deleteFolderId.value;
      var params = null;
      params = "id="+folderId;
      doIt('deleteFolder',params)
      return false;
    }   
    
    function doProtectFolderCall(f)
    {
      var folderId = f.protectFolderId.value;
      var params = null;
      params = "id="+folderId;
      doIt('protectFolder',params)
      return false;
    }   
    
    function doUnProtectFolderCall(f)
    {
      var folderId = f.unProtectFolderId.value;
      var params = null;
      params = "id="+folderId;
      doIt('unprotectFolder',params)
      return false;
    }   
    
    function doDeleteAssetCall(f)
    {
      var assetName = f.deleteAssetName.value;
      var params = "name="+assetName;
      doIt('deleteAsset',params)
      return false;
    }
    
    function doFindAssetsByNameCall(f)
    {
      var assetName = f.findAssetsName.value;
      var params = "name="+assetName;
      doIt('findAssetsByName',params)
      return false;
    }   
    
    function doFindAssetsByTagCall(f)
    {
      var assetTag = f.deleteAssetTag.value;
      var tagValue = f.deleteTagValue.value;
      var params = "tag="+assetTag+"&value="+tagValue;
      doIt('findAssetsByTag',params)
      return false;
    }
    
    function doProtectAssetCall(f)
    {
      var protectAssetName = f.protectAssetName.value;
      var params = null;
      params = "name="+protectAssetName;
      doIt('protectAsset',params)
      return false;
    }   
    
    function doUnProtectAssetCall(f)
    {
      var unProtectAssetName = f.unProtectAssetName.value;
      var params = null;
      params = "name="+unProtectAssetName;
      doIt('unprotectAsset',params)
      return false;
    }   
    
    function doMoveAssetToFolderCall(f)
    {
      var moveAssetName = f.moveAssetName.value;
      var moveToFolderId = f.moveToFolderId.value;
      var params = "name="+moveAssetName+"&toid="+moveToFolderId;
      doIt('moveAsset',params)
      return false;
    }
    
    function doRenameAssetCall(f)
    {
      var assetName = f.assetName.value;
      var assetToName = f.assetToName.value;
      var params = "name="+assetName+"&toname="+assetToName;
      doIt('renameAsset',params)
      return false;
    }