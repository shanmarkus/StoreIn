
// Use Parse.Cloud.define to define as many cloud functions as you want.
// For example:
Parse.Cloud.define("hello", function(request, response) {
  response.success("Hello world!");
});

// Finding Average Stars
Parse.Cloud.define("averageRatings", function(request, response) {
  var query = new Parse.Query("ItemReview");
  query.equalTo("itemId", request.params.itemId);
  query.find({
    success: function(results) {
      var sum = 0;
      for (var i = 0; i < results.length; ++i) {
        sum += results[i].get("rating");
      }
      response.success(sum / results.length);
    },
    error: function() {
      response.error("item lookup failed");
    }
  });
});

// Saving to the Table
Parse.Cloud.define("SaveReviewAverage",function(request, response) {
  var query = new Parse.Query("Item");
  var objId = request.object.get("itemId");
  var rating = request.params.rating;
  query.equalTo("objectId", objId);
  query.find({
    success: function(results) {
      var obj = results[0];
      obj.set("rating", 188);
      obj.save(null,{
        success: function (object) {
          response.success(object);
        },
        error: function (object, error) {
          response.error(error);
        }
      });
    },
    error: function(error) {
      console.error("Got an error " + error.code + " : " + error.message);
    }
  });
});


// Calculating after save item Review
Parse.Cloud.afterSave("ItemReview", function(request, response) {
  var objId = request.object.get("itemId");
  Parse.Cloud.run("averageRatings",{ itemId: objId },{
    sucess: function(results) {

      response.success(results);

    },
    error: function(results, error) {
      response.error(errorMessageMaker("running chained function",error));
    }
  });
});
