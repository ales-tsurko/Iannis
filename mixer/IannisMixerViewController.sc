IannisMixerViewController : CompositeView {
    var channels, 
    numberOfChannels,
    tracksStack;

    *new {arg numberOfChannels = 99;
        ^super.new.init(numberOfChannels);
    }

    init {arg numOfCh;
        var masterChannel = IannisMixerTrackViewController("Master", true);
        numberOfChannels = numOfCh;
        channels = []!numberOfChannels;

        numberOfChannels.do({arg n;
            var name = "Track" + (n+1);
            var channel = IannisMixerTrackViewController(name);
            channel.background = Color.rand(0.77, 0.85);
            channels = channels.add(channel);
        });

        tracksStack = ScrollView();
        tracksStack.canvas = CompositeView();
        tracksStack.canvas.layout = HLayout(*channels);
        tracksStack.canvas.layout.spacing = 0;

        this.layout = HLayout(
            masterChannel,
            [tracksStack, stretch: 1]
        );
    }
}
