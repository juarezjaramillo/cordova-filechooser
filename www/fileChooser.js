module.exports = {
    pick: function (type, success, failure) {
        cordova.exec(success, failure, "FileChooser", "pick", [type]);
    }
};
