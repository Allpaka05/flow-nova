#!/bin/bash

APP_ID="ua.edu.zunu.flownova"

./package-linux.sh

APPIMAGETOOL_URL="https://github.com/AppImage/appimagetool/releases/download/continuous/appimagetool-x86_64.AppImage"
JDK_URL="https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.6%2B10/OpenJDK17U-jdk_x64_linux_hotspot_17.0.6_10.tar.gz"

rm -rf AppDir
mkdir -p AppDir/usr/bin
mkdir -p AppDir/usr/app
mkdir -p AppDir/usr/runtime
mkdir -p AppDir/usr/share/metainfo
mkdir -p AppDir/usr/share/applications

echo "Downloading AppImageTool..."
wget -q --show-progress --progress=bar:force --continue -O appimagetool-x86_64.AppImage "$APPIMAGETOOL_URL"
chmod +x appimagetool-x86_64.AppImage

echo "Downloading Jdk17..."
wget -q --show-progress --progress=bar:force --continue -O jdk17.tar.gz "$JDK_URL"
tar -xf jdk17.tar.gz -C AppDir/usr/runtime --strip-components=1

# Copy app
cp FlowNova.jar AppDir/usr/app/

# Create app executable
cat > AppDir/usr/bin/FlowNova <<'EOF'
#!/bin/sh
HERE="$(dirname "$(readlink -f "$0")")"
exec "$HERE/../runtime/bin/java" -jar "$HERE/../app/FlowNova.jar" "$@"
EOF
chmod +x AppDir/usr/bin/FlowNova

# Create AppImage entrypoint
cat > AppDir/AppRun <<'EOF'
#!/bin/sh
HERE="$(dirname "$(readlink -f "$0")")"
exec "$HERE/usr/bin/FlowNova" "$@"
EOF
chmod +x AppDir/AppRun

# Create desktop entry
cat > "AppDir/usr/share/applications/${APP_ID}.desktop" <<'EOF'
[Desktop Entry]
Type=Application
Name=FlowNova
Exec=FlowNova
Icon=FlowNova
Categories=Education;
Terminal=false
EOF
ln -sf "usr/share/applications/${APP_ID}.desktop" "AppDir/${APP_ID}.desktop"

# Copy icons
cp src/icons/app_icon.svg AppDir/FlowNova.svg
cp src/icons/app_icon256.png AppDir/FlowNova.png
ln -sf FlowNova.png AppDir/.DirIcon

cat > "AppDir/usr/share/metainfo/${APP_ID}.appdata.xml" <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<component type="desktop-application">
	<id>${APP_ID}</id>
	<metadata_license>CC0-1.0</metadata_license>
	<project_license>GPL-3.0</project_license>
	<name>FlowNova</name>
	<summary>Flowchart editor</summary>
	<description>
		<p>FlowNova is a desktop application designed for efficient creation and management of flowcharts. It provides a flexible canvas for designing algorithms, system processes, and logical structures with customizable styling and export capabilities.</p>
	</description>
	<launchable type="desktop-id">${APP_ID}.desktop</launchable>
	<url type="homepage">https://github.com/Allpaka05/flow-nova</url>
    <provides>
        <binary>FlowNova</binary>
    </provides>
	<content_rating type="oars-1.1" />
	<developer id="ua.edu.zunu">
        <name>FlowNova Contributors</name>
    </developer>
</component>
EOF

# Build AppImage
ARCH=x86_64 ./appimagetool-x86_64.AppImage AppDir FlowNova-x86_64.AppImage

# Clean
rm -f appimagetool-x86_64.AppImage
rm -f jdk17.tar.gz
