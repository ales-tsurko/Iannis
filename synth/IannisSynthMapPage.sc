IannisSynthMapPage : CompositeView {
  var <parentSynthController,
  <>availableParameters,
  parametersMenuButton,
  <parametersListView, 
  parametersViews,
  addParameterButton;

  *new {arg parentSynthController;
    ^super.new.init(parentSynthController);
  }

  init {arg parent;
    var currentPreset = parent
    .presetsManagerController
    .presetsManager
    .currentPreset;

    parentSynthController = parent;
    parametersViews = [];

    this.initParametersMenuButton();
    this.initParametersListView();
    this.initAddParameterButton();

    this.layout = VLayout(
      HLayout(nil, parametersMenuButton),
      nil
    );

    this.onLoadPreset(currentPreset);
  }

  initParametersMenuButton {
    parametersMenuButton = Button();
    parametersMenuButton.fixedWidth = 100;
    parametersMenuButton.states = [["Parameters"]];

    parametersMenuButton.action = {arg but;
      this.showParameterChooser();
    };
  }

  initParametersListView {
    var currentPreset = parentSynthController
    .presetsManagerController
    .presetsManager
    .currentPreset;

    parametersListView = ListView();

    this.fetchAvailableParametersFromPreset(currentPreset);
    parametersListView.items = this.availableParameters;

    parametersListView.selectionMode = \extended;
    parametersListView.action = {arg view;
      if (view.selection.size == 0) {
        addParameterButton.enabled = false;
      };
      addParameterButton.enabled = true;
    };

    // double click
    parametersListView.mouseDownAction = {arg view, x, y, modifiers, buttonNumber, clickCount;
      if (view.value.notNil) {
        if (buttonNumber == 0 && clickCount == 2) {
          this.addParameterAction();
        };
      };
    };
  }

  initAddParameterButton {
    addParameterButton = Button();

    addParameterButton.fixedWidth = 100;
    addParameterButton.states = [["Add"]];

    addParameterButton.action = {arg button;
      this.addParameterAction();
    };
  }

  addParameterAction {
    parametersListView.selection.do({arg index;
      var key = this.availableParameters[index].asSymbol;
      this.addParameterForKey(key);
    });

    // remove just added parameter from the list
    availableParameters = availableParameters.reject({arg item, n;
      parametersListView.selection.includes(n);
    });

    // update parameters list
    parametersListView.items = availableParameters;
  }

  showParameterChooser {
    var screenCenter = Rect().centerIn(Window.screenBounds);
    var rect = Rect(
      screenCenter.x-100, 
      screenCenter.y-150,
      200,
      300
    );
    var window = Window("Parameters", rect, false);

    parametersMenuButton.enabled = false;

    this.initParametersListView();
    this.initAddParameterButton();

    window.layout = VLayout(parametersListView, HLayout(nil, addParameterButton));
    window.alwaysOnTop = true;
    window.front;
    window.endFullScreen;
    window.onClose = {parametersMenuButton.enabled = true};
  }

  addParameterForKey {arg key;
    var view = IannisSynthMapParameter(key, key.asString, this);
    parametersViews = parametersViews.add(view);
    this.layout.insert(view, this.layout.children.size);
  }

  fetchAvailableParametersFromPreset {arg preset;
    preset!?{
      availableParameters = preset.values.keys.asArray;
      preset.map!?{
        preset.map.keysDo({arg key;
          // make the parameter unavalaible if it's presented in the map
          availableParameters.removeAt(
            availableParameters.indexOf(key)
          );
        });
      };
    }??{
      availableParameters = [];
    };
  }

  onLoadPreset {arg preset;
    preset!?{
      // clean up view
      parametersViews.do({arg view; view.remove()});
      this.fetchAvailableParametersFromPreset(preset);

      preset.map!?{
        preset.map.keysDo({arg key;
          this.addParameterForKey(key);

          // update parameters list view
          parametersListView!?{
            parametersListView.items = availableParameters;
          };
        });

        parametersViews.do({arg view;
          view.onLoadPreset(preset);
        });
      }
    }
  }
}
