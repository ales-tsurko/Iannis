IannisPresetsManagerController : CompositeView {
  var <presetsManager,
  <parentController,
  <presetsMenu, 
  <updateButton, 
  <newButton, 
  <removeButton,
  <previousButton,
  <nextButton;
  
  *new {arg parentController;
    ^super.new.init(parentController);
  }

  init {arg viewController;
    parentController = viewController;
    this.initPresetsMenu();
    this.initUpdateButton();
    this.initNewButton();
    this.initRemoveButton();
    this.initPreviousButton();
    this.initNextButton();

    presetsManager = IannisPresetsManager(this);

    this.layout = HLayout(
      newButton, 
      updateButton, 
      removeButton,
      presetsMenu,
      previousButton,
      nextButton,
      nil
    );
  }

  initPresetsMenu {
    presetsMenu = PopUpMenu();
    presetsMenu.fixedWidth = 200;

    presetsMenu.action = {arg popup;
      this.presetsManager.loadPreset(popup.value);
    };
  }

  initUpdateButton {
    updateButton = Button();
    updateButton.fixedWidth = 24;
    updateButton.fixedHeight = 24;
    updateButton.states = [["↻"]];

    updateButton.action = {arg but;
      if (but.value == 0) {
        this.presetsManager.updateSelectedPresetWithCurrentData();
      };
    };
  }

  initNewButton {
    newButton = Button();
    newButton.fixedWidth = 24;
    newButton.fixedHeight = 24;
    newButton.states = [["💾"]];

    newButton.action = {arg but;
      if (but.value == 0) {
        this.showSavePresetDialog();
      };
    };
  }

  showSavePresetDialog {
    var screenBounds = Window.screenBounds();
    var rect = Rect(
      screenBounds.width/2-135,
      screenBounds.height/2-50,
      270,
      100
    );
    var window = Window("New Preset", rect, false);
    var nameField = TextField();
    var okButton = Button();
    var cancelButton = Button();
    okButton.fixedWidth = 90;
    cancelButton.fixedWidth = 90;
    okButton.states = [["OK"]];
    cancelButton.states = [["Cancel"]];

    okButton.action = {arg but;
      if (but.value == 0) {
        var preset = this.presetsManager.currentPreset;
        var presetsPath = this.presetsManager.userPresetsPath;
        preset.name = nameField.value;
        preset.isFactory = false;

        // add preset
        this.presetsManager.addPreset(preset);

        // update the presets file
        this.presetsManager.writeUserPresetsToDisk(presetsPath);

        // load just saved preset
        this.presetsMenu.valueAction = this.presetsMenu.items.size - 1;

        // close the window
        window.close();
      };
    };

    cancelButton.action = {arg but;
      if (but.value == 0) {
        window.close();
      };
    };

    // layout
    window.layout = VLayout(
      nameField,
      HLayout(
        okButton,
        cancelButton
      )
    );

    window.front();
  }

  initRemoveButton {
    removeButton = Button();
    removeButton.fixedWidth = 24;
    removeButton.fixedHeight = 24;
    removeButton.states = [["⌦"]];

    removeButton.action = {arg but;
      if (but.value == 0) {
        var presetsPath = this.presetsManager.userPresetsPath;
        this.presetsManager.removePreset(this.presetsMenu.value); 
        this.presetsMenu.removeAt(this.presetsMenu.value);
        // update the presets file
        this.presetsManager.writeUserPresetsToDisk(presetsPath);
      };
    };
  }

  initPreviousButton {
    previousButton = Button();
    previousButton.fixedWidth = 24;
    previousButton.fixedHeight = 24;
    previousButton.states = [["←"]];

    previousButton.action = {arg but;
      if (but.value == 0) {
        var currentIndex = this.presetsMenu.value;
        var newIndex = (currentIndex - 1).clip(0, this.presetsMenu.items.size-1);
        this.presetsMenu.valueAction = newIndex;
      };
    };
  }

  initNextButton {
    nextButton = Button();
    nextButton.fixedWidth = 24;
    nextButton.fixedHeight = 24;
    nextButton.states = [["→"]];

    nextButton.action = {arg but;
      if (but.value == 0) {
        var currentIndex = this.presetsMenu.value;
        var newIndex = (currentIndex + 1).clip(0, this.presetsMenu.items.size-1);
        this.presetsMenu.valueAction = newIndex;
      };
    };
  }

  parentControllerDidFinishParsing {
    var id = this.parentController.metadata[\id];
    var dir = SynthDef.synthDefDir +/+ id;
    var name = this.parentController.metadata[\name].toLower.replace(" ", "_");
    var path = dir +/+ name ++ ".presets";

    if (File.exists(path)) {
     this.presetsManager.loadUserPresetsFromDisk(path);
     // add user presets to the menu
     this.presetsManager.getUserPresets.do({arg preset;
       this.presetsMenu.items = this.presetsMenu.items.add(preset.name);
     });
   } {
     // if there is now resources dir for the synth -- create it
     if (File.exists(dir).not) {
       File.mkdir(dir);
     };
     this.presetsManager.writeUserPresetsToDisk(path);
   };
  }
}
