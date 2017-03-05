IannisProbabilisticSequencerMultipleStepsView : ScrollView {
	var data;

	*new {arg numberOfSteps;
		^super.new.init(numberOfSteps);
	}

	init {arg numberOfSteps;
		this.canvas = CompositeView();

		this.hasHorizontalScroller = true;
		this.hasVerticalScroller = false;
		this.hasBorder = false;

		data = Array.fill(128, {
			var dict = Dictionary.new;
			dict[\pitchIndex] = 60;
			dict[\probability] = 0;
		});

		this.updateSteps(numberOfSteps);

		this.minHeight = 300;
	}

	updateSteps {arg numberOfSteps;
		// initialize sliders
		this.canvas.layout = GridLayout.rows(
			Array.fill(numberOfSteps, {arg n;
				var ch = IannisProbabilisticSequencerStepView.new(n+1);
				ch.fixedWidth = 80;

				if(data[n].notNil, {
					ch.probabilitySlider.valueAction = data[n][\probability];
					ch.pitchPopUp.valueAction = data[n][\pitchIndex];
				});

				// return
				ch;
			});
		);
	}

	updateData {
		canvas.children.do({arg item;
			if(item.isKindOf(IannisProbabilisticSequencerStepView), {
				var n = item.label.string.asInt - 1;
				data[n][\probability] = item.probabilitySlider.value;
				data[n][\pitchIndex] = item.pitchPopUp.value;
			});
		});
	}

}