package endfield.ui.markdown;

import arc.scene.event.SceneEvent;

public class UrlClickedEvent extends SceneEvent {
	public String clickedUrl;

	public UrlClickedEvent(String url) {
		clickedUrl = url;
	}
}
