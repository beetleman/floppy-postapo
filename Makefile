build-deploy:
	lein do clean, cljsbuild once min
release: build-deploy
	rm -rf release
	cp -r ./resources/public release
