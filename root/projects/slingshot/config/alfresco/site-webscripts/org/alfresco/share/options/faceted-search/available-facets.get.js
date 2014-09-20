// By default this options WebScript defines a hard-coded set of QName options to use when configuring
// facets for search, but this can be overridden to change the hard-coded values or to retrieve options
// from Solr...
function getAvailableFacetProperties() {
   var availableFacets = [];
   var result = remote.call("/api/facet/facetable-properties");
   if (result.status.code == status.STATUS_OK)
   {
      var rawData = JSON.parse(result);
      if (rawData && rawData.data && rawData.data.properties)
      {
         var properties = rawData.data.properties;
         for (var i=0; i<properties.length; i++)
         {
            availableFacets.push({
               value: properties[i].longqname,
               label: properties[i].displayName
            });
         }
      }
   }
   return availableFacets;
}
model.items = getAvailableFacetProperties();