package cn.iecas.sampleset.pojo.dto;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Manifest {
    private List<Data> data;
    private Datasetinfo datasetinfo;

    public Manifest split(Manifest.Data data){
        Manifest manifest = new Manifest();
        manifest.setDatasetinfo(JSONObject.parseObject(this.datasetinfo.toString()));
        manifest.setData(data);
        return manifest;
    }

    public Datasetinfo getDatasetinfo() {
        return datasetinfo;
    }

    public void setDatasetinfo(JSONObject datasetinfoJSON) {
        datasetinfo = JSONObject.parseObject(datasetinfoJSON.toJSONString(), Datasetinfo.class);
    }

    public List<Data> getData() {
        return data;
    }

    public void setData(JSONArray data) {
        this.data = data == null ? null : JSONArray.parseArray(data.toJSONString(), Data.class);
    }

    public void setData(Data data){
        this.data.add(data);
    }


    public static class Datasetinfo {
        private int id;
        private int projectId;
        private String creator;
        private String keywords;
        private Date createdTime;
        private String description;
        private String version = "1.0";


        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Date getCreatedTime() {
            return createdTime;
        }

        public void setCreatedTime(Date createdTime) {
            this.createdTime = createdTime;
        }

        public String getCreator() {
            return creator;
        }

        public void setCreator(String creator) {
            this.creator = creator;
        }

        public String getKeywords() {
            return keywords;
        }

        public void setKeywords(String keywords) {
            this.keywords = keywords;
        }

        public int getProjectId() {
            return projectId;
        }

        public void setProjectId(int projectId) {
            this.projectId = projectId;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }
    }

    @XStreamAlias("annotation")
    public static class Data {

        @XStreamAlias("source")
        private Source source = new Source();
        @XStreamAlias("objects")
        private Objects objects = new Objects();
        @XStreamAlias("segmentation")
        private Segmentation segmentation = new Segmentation();

        public Source getSource() {
            return source;
        }

        public void setSource(JSONObject source) {
            this.source = JSONObject.parseObject(source.toJSONString(),Source.class);
        }

        public Objects getObjects() {
            return objects;
        }

        public void setObjects(JSONObject objects) {
            this.objects = JSONObject.parseObject(objects.toJSONString(),Objects.class);
        }

        public Segmentation getSegmentation() {
            return segmentation;
        }

        public void setSegmentation(JSONObject segmentation) {
            this.segmentation = JSONObject.parseObject(segmentation.toJSONString(), Segmentation.class);
        }

        public static class Objects{
            @XStreamImplicit
            List<ObjectInfo> object = new ArrayList<>();


            public List<ObjectInfo> getObject() {
                return object;
            }

            public void setObject(JSONArray object) {
                this.object = JSONArray.parseArray(object.toJSONString(),ObjectInfo.class);
            }


            @XStreamAlias("object")
            public static class ObjectInfo{
                private int id;
                private String type;
                private String coordinate;
                private String description;
                private Points points;
                @XStreamImplicit
                private List<PossibleResult> possibleResult;


                public int getId() {
                    return id;
                }

                public void setId(int id) {
                    this.id = id;
                }

                public String getType() {
                    return type;
                }

                public void setType(String type) {
                    this.type = type;
                }

                public String getCoordinate() {
                    return coordinate;
                }

                public void setCoordinate(String coordinate) {
                    this.coordinate = coordinate;
                }

                public String getDescription() {
                    return description;
                }

                public void setDescription(String description) {
                    this.description = description;
                }

                public Points getPoints() {
                    return points;
                }

                public void setPoints(JSONObject points) {
                    this.points = JSONObject.parseObject(points.toJSONString(),Points.class);
                }

                public List<PossibleResult> getPossibleResult() {
                    return possibleResult;
                }

                public void setPossibleResult(JSONArray possibleResult) {
                    this.possibleResult = JSONArray.parseArray(possibleResult.toJSONString(),PossibleResult.class);
                }

                @XStreamAlias("possibleResult")
                public static class PossibleResult{
                    private String name;
                    private String probability;

                    public String getName() {
                        return name;
                    }

                    public void setName(String name) {
                        this.name = name;
                    }

                    public String getProbability() {
                        return probability;
                    }

                    public void setProbability(String probability) {
                        this.probability = probability;
                    }

                }

                public static class Points{
                    @XStreamImplicit(itemFieldName = "point")
                    private List<String> point;

                    public List<String> getPoint() {
                        return point;
                    }

                    public void setPoint(JSONArray points) {
                        this.point = JSONArray.parseArray(points.toJSONString(),String.class);
                    }

                    @XStreamAlias("point")
                    public static class Point{
                        private double lng;
                        private double lat;

//                        public Point(String point){
//                            String[] lonLat = point.split(",");
//                            this.lng = Double.parseDouble(lonLat[0]);
//                            this.lat = Double.parseDouble(lonLat[1]);
//                        }
                        public double getLng() {
                            return lng;
                        }

                        public void setLng(double lng) {
                            this.lng = lng;
                        }

                        public double getLat() {
                            return lat;
                        }

                        public void setLat(double lat) {
                            this.lat = lat;
                        }

                    }
                }

            }
        }



        public static class Source {
            private int id;
            private String filename;
            private String origin;

            public int getId() {
                return id;
            }

            public void setId(int id) {
                this.id = id;
            }

            public String getFilename() {
                return filename;
            }

            public void setFilename(String filename) {
                this.filename = filename;
            }

            public String getOrigin() {
                return origin;
            }

            public void setOrigin(String origin) {
                this.origin = origin;
            }
        }


        @XStreamAlias("segmentation")
        public static class Segmentation {
            private List<String> resultfile = new ArrayList<>();

            public List<String> getResultfile() {
                return resultfile;
            }

            public void setResultfile(List<String> resultfile) {
                this.resultfile = resultfile;
            }
        }
    }
}
