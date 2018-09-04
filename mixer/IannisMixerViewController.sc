IannisMixerViewController : CompositeView {
    var <channels, 
    numberOfChannels,
    tracksStack,
    <masterChannel,
    soloedChannelsIndices,
    mutedChannelsIndices;

    *new {arg numberOfChannels = 16;
        ^super.new.init(numberOfChannels);
    }

    init {arg numOfCh;
        masterChannel = IannisMixerTrackViewController("Master", true, this, 0);
        numberOfChannels = numOfCh;
        channels = []!numberOfChannels;
        soloedChannelsIndices = [];
        mutedChannelsIndices = [];

        numberOfChannels.do({arg n;
            var name = "Track" + (n+1);
			var hasInstrumentChooser = not((n%numberOfChannels == 0).or(n%numberOfChannels == 1));
            var channel = IannisMixerTrackViewController(name, false, this, n+1, hasInstrumentChooser);
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

        // we transfered ownership of channels to HLayout
        // so here we're returning it back
        this.initChannelsArray();
    }

    initChannelsArray {
        var result = [];
        tracksStack.canvas.children.do({arg item;
            result = result.add(item);
        });
        channels = result;
    }

}

// track view controller delegate methods
+ IannisMixerViewController {
    didToggleMuteAtChannel{arg channel, isMute;
        if((channel.index != 0) && (soloedChannelsIndices.size > 0)) {
            if(isMute){this.unsoloChannel(channel);channel.setSolo(false)};
            if(isMute.not){this.soloChannel(channel);channel.setSolo(true)};
        };

        if(soloedChannelsIndices.size < 1) {
            if(isMute){
                mutedChannelsIndices = mutedChannelsIndices.add(channel.index);
            } {
                mutedChannelsIndices.remove(channel.index);
            };
        }
    }

    didToggleSoloAtChannel{arg channel, isSolo;
        if(isSolo){
            this.soloChannel(channel);
        } {
            this.unsoloChannel(channel);
        };
    }

    soloChannel {arg channel;
        var tracks = [this.masterChannel] ++ this.channels;
        soloedChannelsIndices = soloedChannelsIndices.add(channel.index);
        if(channel.mixerTrack.isMute) {channel.setMute(false)};
        tracks.do({arg track;
            if(track.index != 0 && soloedChannelsIndices.includes(track.index).not){
                track.setMute(true);
            }
        });
    }

    unsoloChannel {arg channel;
        soloedChannelsIndices.remove(channel.index);
        if(soloedChannelsIndices.size > 0) {
            channel.setMute(true);
        } {
            this.unsoloLastChannel();
        };
    }

    unsoloLastChannel {
        var tracks = [this.masterChannel] ++ this.channels;
        numberOfChannels.do({arg n;
            var nonMasterTrackIndex = n+1;
            if(mutedChannelsIndices.includes(nonMasterTrackIndex).not) {
                tracks[nonMasterTrackIndex].setMute(false);
            };
        });
    }
}
