IannisProbabilisticSequencerMultipleStepsView : ScrollView {
	var <label, <numOfSteps, <parentController, 
  closeButton;

	*new {arg numberOfSteps, parentController, showCloseButton = true;
		^super.new.init(numberOfSteps, parentController, showCloseButton);
	}

	init {arg numberOfSteps, parentCtrlr, showCloseButton;
    parentController = parentCtrlr;
    numOfSteps = numberOfSteps;

		label = StaticText.new;
		label.string = parentController.name;

		this.canvas = CompositeView();

		this.hasHorizontalScroller = false;
		this.hasVerticalScroller = true;
		this.hasBorder = false;
		this.autohidesScrollers = false;

		this.updateSteps(numberOfSteps);

		this.minHeight = 200;
		this.minWidth = 940;

    ~views = [];
    
    // add close button 
    if (showCloseButton) {
      closeButton = Button.new;
      closeButton.fixedWidth = 18;
      closeButton.fixedHeight = 18;
      closeButton.states = [["✖"]];

      closeButton.action = {arg button;
        if (button.value == 0) {
          parentController.closeButtonAction(button);
        };
      };

      ~views = ~views.add(closeButton);
    };

    ~views = ~views.add(label);
    ~views = ~views.add(nil);

		this.layout = VLayout(HLayout(*~views),nil);
	}

  updateSteps {arg numberOfSteps;
    numOfSteps = numberOfSteps;

    this.canvas.layout = GridLayout();
    this.canvas.layout.vSpacing = 30;

    numberOfSteps.do({arg n;
      var ch = IannisProbabilisticSequencerStepView.new(n+1, n, this);

      ch.probabilitySlider.valueAction = parentController.data[\probability][n];
      ch.expressionField.valueAction = parentController.data[\expression][n];

      this.canvas.layout.add(ch, floor(n/4), n%4)
    });

    // update pattern and number of steps in parent controller
    parentController.numberOfSteps = numOfSteps;
    parentController.updatePattern();
  }

  updateParentData {
    canvas.children.do({arg item;
      if(item.isKindOf(IannisProbabilisticSequencerStepView), {
        var n = item.number;
        parentController.data[\probability][n] = item.probabilitySlider.value;
        parentController.data[\expression][n] = item.expressionField.string;
        parentController.data[\realExpression][n] = item.realExpression;
      });
    });
  }
}
