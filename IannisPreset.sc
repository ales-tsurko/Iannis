IannisPreset {
  var <name, <data, <isFactory;

  *new {arg data;
    ^super.new.init(data);
  }

  init {arg theData;
    data = theData;
    name = theData[\name];
    isFactory = theData[\isFactory];
  }
}
