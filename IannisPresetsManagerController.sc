IannisPresetsManagerController : CompositeView {
  var <presetsManager,
  <presetsMenu, 
  <updateButton, 
  <newButton, 
  <removeButton,
  <previousButton,
  <nextButton;
  
  *new {
    ^super.new.init();
  }

  init {
    this.initPresetsMenu();
    this.initUpdateButton();
    this.initNewButton();
    this.initRemoveButton();
    this.initPreviousButton();
    this.initNextButton();

    presetsManager = IannisPresetsManager(this);

    this.layout = HLayout(
      updateButton, 
      newButton, 
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
  }

  initUpdateButton {
    updateButton = Button();
    updateButton.fixedWidth = 24;
    updateButton.fixedHeight = 24;
    updateButton.states = [["↻"]];
  }

  initNewButton {
    newButton = Button();
    newButton.fixedWidth = 24;
    newButton.fixedHeight = 24;
    newButton.states = [["💾"]];
  }

  initRemoveButton {
    removeButton = Button();
    removeButton.fixedWidth = 24;
    removeButton.fixedHeight = 24;
    removeButton.states = [["⌫"]];
  }

  initPreviousButton {
    previousButton = Button();
    previousButton.fixedWidth = 24;
    previousButton.fixedHeight = 24;
    previousButton.states = [["←"]];
  }

  initNextButton {
    nextButton = Button();
    nextButton.fixedWidth = 24;
    nextButton.fixedHeight = 24;
    nextButton.states = [["→"]];
  }
}
