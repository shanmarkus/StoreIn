
// Use Parse.Cloud.define to define as many cloud functions as you want.
// For example:
Parse.Cloud.define("hello", function(request, response) {
  response.success("Hello world!");
});

/* Start ------------------------------------------------ Calculation of User Claimed Promotion */
Parse.Cloud.define("calculateTotalPromotion", function(request, response) {
  var objectId = request.params.objectId;
  var User = Parse.Object.extend("_User");
  var user = new User();
  user.id = objectId;

  var query = new Parse.Query("Activity_User_Claim_Promotion");
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

Parse.Cloud.define("SaveTotalClaimedPromotion",function(request, response) {
  // get Item Id
  var userId = request.params.objectId;
  var totalClaimedPromotion;

  // get total Check in
  var CheckIn = Parse.Cloud.run('calculateTotalPromotion', {objectId:userId},{
    success: function(result) {
    // getting the result
    totalClaimedPromotion = result;
        // query
        var User = Parse.Object.extend("_User");
        var query = new Parse.Query(User);
        query.equalTo("objectId", userId);
        query.find({
          success: function(results) {
            var obj = results[0];
            obj.set("totalClaimedPromotion", totalClaimedPromotion);
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

// Run after save to database

Parse.Cloud.afterSave("Activity_User_Claim_Promotion", function(request, response) {
  var currentUser = Parse.User.current();
  var objId = currentUser.id;

  Parse.Cloud.run("SaveTotalClaimedPromotion",{ objectId: objId},{
    sucess: function(results) {
      response.success(results);
    },
    error: function(results, error) {
      response.error(errorMessageMaker("running chained function",error));
    }
  });
});

/* End ------------------------------------------------ Calculation of User Claimed Promotion */

/* Start ------------------------------------------------ Calculation of Place Check In */
Parse.Cloud.define("calculatePlaceCheckIn", function(request, response) {
  var objectId = request.params.objectPlaceId;
  var Place = Parse.Object.extend("Place");
  var place = new Place();
  place.id = objectId;

  var query = new Parse.Query("Activity_User_Check_In_Place");
  query.equalTo("placeId", place);
  query.find({
    success: function(results) {
      response.success(results.length);
    },
    error: function() {
      response.error("item lookup failed");
    }
  });
});

// Calculating Total Checkin 

Parse.Cloud.define("calculateUserCheckIn", function(request, response) {
  var objectId = request.params.objectId;
  var User = Parse.Object.extend("_User");
  var user = new User();
  user.id = objectId;

  var query = new Parse.Query("Activity_User_Check_In_Place");
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

// Saving to the Table (Total CheckIn)
Parse.Cloud.define("SaveTotalCheckIn",function(request, response) {
  // get Item Id
  var userId = request.params.objectId;
  var placeId = request.params.objectPlaceId;
  var totalCheckIn;
  var totalPlaceCheckIn;

  // get total Check in
  var CheckIn = Parse.Cloud.run('calculateUserCheckIn', {objectId:userId},{
    success: function(result) {
    // getting the result
    totalCheckIn = result;
        // query
        var User = Parse.Object.extend("_User");
        var query = new Parse.Query(User);
        query.equalTo("objectId", userId);
        query.find({
          success: function(results) {
            var obj = results[0];
            obj.set("totalCheckIn", totalCheckIn);
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

  var PlaceCheckIn = Parse.Cloud.run('calculatePlaceCheckIn', {objectPlaceId:placeId},{
    success: function(result) {
    // getting the result
    totalPlaceCheckIn = result;
        // query
        var Place = Parse.Object.extend("Place");
        var query = new Parse.Query(Place);
        query.equalTo("objectId", placeId);
        query.find({
          success: function(results) {
            var obj = results[0];
            obj.set("totalCheckIn", totalPlaceCheckIn);
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

// Calculating after save User Check In to a place
Parse.Cloud.afterSave("Activity_User_Check_In_Place", function(request, response) {
  var currentUser = Parse.User.current();
  var objId = currentUser.id;
  var currentPlace = request.object.get("placeId");
  var placeObjId = currentPlace.id;

  Parse.Cloud.run("SaveTotalCheckIn",{ objectId: objId, objectPlaceId:placeObjId},{
    sucess: function(results) {
      response.success(results);
    },
    error: function(results, error) {
      response.error(errorMessageMaker("running chained function",error));
    }
  });
});

/* End ------------------------------------------------ Calculation of User Check In */

/* Start ------------------------------------------------ Calculation of User Follower and Following  */

// Finding user total follower of current User

Parse.Cloud.define("calculateUserFollower", function(request, response) {
  var objectId = request.params.objectId;
  var User = Parse.Object.extend("_User");
  var user = new User();
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
  var objectId = request.params.objectId;
  var User = Parse.Object.extend("_User");
  var user = new User();
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

// Define User update user

Parse.Cloud.define("updateUserDemographic",function(request, response) {
  // get Item Id
  var userId = request.params.objectId;
  var totalNumberFollower;
  var totalNumberFollowing;

  // get the average ratings
  var numberFollower = Parse.Cloud.run('calculateUserFollower', {objectId:userId},{
    success: function(result) {
        // getting the result
        totalNumberFollower = result;
        var numberFollowing = Parse.Cloud.run('calculateUserFollowing', {objectId:userId},{
          success: function(result) {
            totalNumberFollowing = result;
             // query
             var User = Parse.Object.extend("_User");
             var query = new Parse.Query(User);
             query.equalTo("objectId", userId);
             query.find({
              success: function(results) {
                var obj = results[0];
                obj.set("following", totalNumberFollowing);
                obj.set("follower", totalNumberFollower);
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
},
error: function(error) {
  console.error("Got an error " + error.code + " : " + error.message);
}
});

});

// Calculating after save item Review
Parse.Cloud.afterSave("Rel_User_User", function(request, response) {
  var currentUser = Parse.User.current();
  var objId = currentUser.id;
  Parse.Cloud.run("updateUserDemographic",{ objectId: objId },{
    sucess: function(results) {
      response.success(results);
    },
    error: function(results, error) {
      response.error(errorMessageMaker("running chained function",error));
    }
  });
});

/* End ------------------------------------------------ Calculation of User Follower and Following  */

/* Start ------------------------------------------------ Calculation of Average Rating Review Item  */

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

/* End ------------------------------------------------ Calculation of Average Rating Review Item  */


