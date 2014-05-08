
// Use Parse.Cloud.define to define as many cloud functions as you want.
// For example:
Parse.Cloud.define("hello", function(request, response) {
  response.success("Hello world!");
});

// Finding user total follower of current User

Parse.Cloud.define("calculateUserFollower", function(request, response) {
  var User = Parse.Object.extend("_User");
  var user = new User();
  var objectId = request.params.objectId;
  user.id = objectId;

  var query = new Parse.Query("Rel_User_User");
  query.equalTo("followingId", user);
  query.find({
    success: function(results) {
      response.success(results.length);
    },
    error: function() {
      response.error("item lookup failed");
    }
  });
});

// Finding user total number of user that the current user following 

Parse.Cloud.define("calculateUserFollowing", function(request, response) {
  var User = Parse.Object.extend("_User");
  var user = new User();
  var objectId = request.params.userId;
  user.id = objectId;

  var query = new Parse.Query("Rel_User_User");
  query.equalTo("userId", user);
  query.find({
    success: function(results) {
      response.success(results.length);
    },
    error: function() {
      response.error("item lookup failed");
    }
  });
});

// After Save User adding user

Parse.Cloud.afterSave("Rel_User_User", function(request, response) {
  var currentUser = Parse.User.current();
  var userId = currentUser.id;

  var numberFollower = Parse.Cloud.run('calculateUserFollower', {objectId:userId},{
    sucess: function(results) {
      // if no error do find the number followeing
      var numberFollower = Parse.Cloud.run('calculateUserFollowing', {objectId:userId},{
        sucess: function(results) {
          // do nothing
          
        },
        error: function(results, error) {
          response.error(errorMessageMaker("running chained function",error));
        }
      });
    },
    error: function(results, error) {
      response.error(errorMessageMaker("running chained function",error));
    }
  });
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
  // get Item Id
  var objId = request.params.objectId;
  var AverageRating;

  // get the average ratings
  var rating = Parse.Cloud.run('averageRatings', {itemId:objId},{
    success: function(result) {
    // getting the result
    AverageRating = result;
        // query
        var Item = Parse.Object.extend("Item");
        var query = new Parse.Query(Item);
        query.equalTo("objectId", objId);
        query.find({
          success: function(results) {
            var obj = results[0];
            obj.set("rating", AverageRating);
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
      },
      error: function(error) {
        console.error("Got an error " + error.code + " : " + error.message);
      }
    });

});


// Calculating after save item Review
Parse.Cloud.afterSave("ItemReview", function(request, response) {
  var objId = request.object.get("itemId");
  Parse.Cloud.run("SaveReviewAverage",{ objectId: objId },{
    sucess: function(results) {
      response.success(results);
    },
    error: function(results, error) {
      response.error(errorMessageMaker("running chained function",error));
    }
  });
});



