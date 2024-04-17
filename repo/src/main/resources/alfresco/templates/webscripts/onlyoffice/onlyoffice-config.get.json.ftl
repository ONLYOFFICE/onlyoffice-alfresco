{
	"url":"${(settings['url']!"")?json_string}",
	"security": {
		"key": ${(settings['security.key']!"")?json_string}"
	},
	"innerUrl":"${(settings['innerUrl']!"")?json_string}",
	"productInnerUrl":"${(settings['productInnerUrl']!"")?json_string}",
	"ignoreSSLCertificate":${(settings['ignoreSSLCertificate']!false)?json_string},
	"webpreview": ${webpreview?c},
	"customization": {
		"forcesave": ${(settings['customization.forcesave']!false)?json_string},
		"chat": ${settings['customization.chat']},
		"help": ${settings['customization.help']},
		"compactHeader": ${(settings['customization.compactHeader']!false)?json_string},
		"toolbarNoTabs": ${(settings['customization.toolbarNoTabs']!false)?json_string},
		"feedback": ${(settings['customization.feedback']!false)?json_string},
		"reviewDisplay": ${settings['customization.review.reviewDisplay']}
	}
}
