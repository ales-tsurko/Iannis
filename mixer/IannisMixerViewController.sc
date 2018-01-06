IannisMixerViewController : ScrollView {
    var channels, 
    numberOfChannels;

    *new {arg numberOfChannels = 99;
        ^super.new.init(numberOfChannels);
    }

    init {arg numOfCh;
        numberOfChannels = numOfCh;
        channels = []!numberOfChannels;

        numberOfChannels.do({
            var channel = IannisMixerTrackViewController();
            channel.background = Color.rand(0.3, 0.9);
            channels = channels.add(channel);
        });

        this.canvas = CompositeView();
        this.canvas.layout = HLayout(*channels);
    }
}
