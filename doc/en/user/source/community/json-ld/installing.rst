JSON-LD installation
====================
  
 #. Download the plugin from the `nightly GeoServer community module builds <https://build.geoserver.org/geoserver/master/community-latest/>`_.

    .. warning:: Make sure to match the version of the extension to the version of the GeoServer instance!

 #. The plugin name follows the pattern:
    ::

      geoserver-X.XX-SNAPSHOT-json-ld-plugin.zip

 #. Extract the contents of the archive into the ``WEB-INF/lib`` directory of the GeoServer installation.

Validating that the plugin was correctly installed
--------------------------------------------------

If the plugin was correctly installed JSON-LD will be available as an output format. This check can be done by accessing the ``Layer Preview`` page in GeoServer web  UI and confirm that JSON-LD is one of the available formats: 

.. figure:: images/json-ld_preview.png

   Dropdown menu in ``Layer Preview`` page with the JSON-LD format highlighted.